package edu.limkokwing.marketflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.nio.file.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real Swing Action/Document/Change events on EDT and Robot screenshots; no mocked UI. */
@EnabledIfEnvironmentVariable(named="RUN_GUI_TESTS",matches="true")
class GuiTest {
    @Test void fullCheckoutAndScreenshots()throws Exception{
        Database db=StoreTest.testDatabase();
        try(Connection c=db.open();Statement s=c.createStatement()){
            s.executeUpdate("DELETE FROM sale_items");s.executeUpdate("DELETE FROM sales");s.executeUpdate("DELETE FROM products");
            String seed=Files.readString(Path.of("database/02-seed.sql"));
            seed=seed.substring(seed.indexOf("INSERT IGNORE"));s.executeUpdate(seed);
        }
        AtomicReference<MainFrame> ref=new AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{try{App.configureUI();}catch(Exception e){throw new RuntimeException(e);}MainFrame f=new MainFrame(new Store(db));f.setVisible(true);ref.set(f);});MainFrame f=ref.get();
        try{
            awaitIdle(f);assertEquals(12,f.productModel.getRowCount());capture(f,"01-checkout.png");
            SwingUtilities.invokeAndWait(()->{f.search.setText("rice");assertEquals(1,f.products.getRowCount());f.products.setRowSelectionInterval(0,0);findButton(f,"Add to cart").doClick();assertEquals(1,f.cartModel.getRowCount());f.discount.setValue(10);f.tendered.setText("200.00");f.search.setText("");});
            capture(f,"02-cart.png");
            SwingUtilities.invokeAndWait(()->{
                javax.swing.Timer confirm=new javax.swing.Timer(100,e->{for(Window w:Window.getWindows())if(w instanceof JDialog d && d.isVisible() && d.getTitle().equals("Confirm checkout")){JButton yes=findButton(d,"Yes");if(yes!=null){yes.doClick();((javax.swing.Timer)e.getSource()).stop();}}});
                confirm.start();findButton(f,"Complete sale  /  Ctrl+Enter").doClick();
            });
            awaitIdle(f);assertNotNull(f.lastReceipt());assertEquals("166.50",f.lastReceipt().sale().total().toPlainString());assertEquals("33.50",f.lastReceipt().sale().change().toPlainString());
            AtomicReference<JDialog> receiptDialog=new AtomicReference<>();
            SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())if(w instanceof JDialog d && d.isVisible())receiptDialog.set(d);});
            assertNotNull(receiptDialog.get());capture(receiptDialog.get(),"03-receipt.png");
            SwingUtilities.invokeAndWait(()->{receiptDialog.get().dispose();f.tabs.setSelectedIndex(1);});
            capture(f,"04-history.png");assertEquals(1,f.salesModel.getRowCount());
            SwingUtilities.invokeAndWait(()->f.tabs.setSelectedIndex(2));capture(f,"05-accessibility.png");
            assertEquals("Search products",f.search.getAccessibleContext().getAccessibleName());
        }finally{SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())w.dispose();});}
    }
    @Test void historyNavigationSortingAndAccessibility()throws Exception{
        Database db=StoreTest.testDatabase();
        try(Connection c=db.open();Statement s=c.createStatement()){
            s.executeUpdate("DELETE FROM sale_items");s.executeUpdate("DELETE FROM sales");s.executeUpdate("DELETE FROM products");
            s.executeUpdate("INSERT INTO products(sku,name,category,price,stock,reorder_level,active) VALUES('GUI-1','GUI item','Test',10,100,1,TRUE)");
            for(int i=0;i<55;i++)s.executeUpdate("INSERT INTO sales(subtotal,discount_percent,total,payment,tendered,change_due) VALUES(10,0,10,'CARD',10,0)");
        }
        AtomicReference<MainFrame> ref=new AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{try{App.configureUI();}catch(Exception e){throw new RuntimeException(e);}MainFrame frame=new MainFrame(new Store(db));frame.setVisible(true);ref.set(frame);});
        MainFrame f=ref.get();
        try{
            awaitIdle(f);
            SwingUtilities.invokeAndWait(()->{
                assertEquals(50,f.salesModel.getRowCount());assertFalse(findButton(f,"Newer sales").isEnabled());assertTrue(findButton(f,"Older sales").isEnabled());
                assertLabel(f,"Discount %",f.discount);assertLabel(f,"Tendered SLE",f.tendered);
                assertEquals("Sales history",f.salesTable.getAccessibleContext().getAccessibleName());
                @SuppressWarnings("unchecked") JComboBox<String> scale=(JComboBox<String>)findNamed(f,"Text size");
                assertNotNull(scale);float original=f.search.getFont().getSize2D();scale.setSelectedIndex(1);assertTrue(f.search.getFont().getSize2D()>original);scale.setSelectedIndex(0);assertEquals(original,f.search.getFont().getSize2D(),0.01f);
                JCheckBox contrast=findCheckbox(f,"High contrast");assertNotNull(contrast);contrast.doClick();assertEquals(Color.BLACK,f.search.getBackground());contrast.doClick();
                f.tabs.setSelectedIndex(1);
            });
            capture(f,"06-history-pagination.png");
            SwingUtilities.invokeAndWait(()->findButton(f,"Older sales").doClick());
            awaitIdle(f);
            SwingUtilities.invokeAndWait(()->{assertEquals(5,f.salesModel.getRowCount());assertFalse(findButton(f,"Older sales").isEnabled());assertTrue(findButton(f,"Newer sales").isEnabled());});
            capture(f,"07-older-history.png");
            SwingUtilities.invokeAndWait(()->findButton(f,"Newer sales").doClick());
            awaitIdle(f);
            SwingUtilities.invokeAndWait(()->{
                assertEquals(50,f.salesModel.getRowCount());
                f.salesTable.getRowSorter().toggleSortOrder(0);
                long previous=-1;
                for(int i=0;i<f.salesTable.getRowCount();i++){long id=(Long)f.salesTable.getValueAt(i,0);assertTrue(id>previous);previous=id;}
                findButton(f,"Older sales").doClick();
            });
            awaitIdle(f);SwingUtilities.invokeAndWait(()->findButton(f,"Refresh history").doClick());awaitIdle(f);
            SwingUtilities.invokeAndWait(()->{assertEquals(50,f.salesModel.getRowCount());assertFalse(findButton(f,"Newer sales").isEnabled());});
        }finally{SwingUtilities.invokeAndWait(f::dispose);}
    }
    private static void assertLabel(Container parent,String text,JComponent target){
        assertTrue(hasLabel(parent,text,target),"Missing label association: "+text);
    }
    private static JComponent findNamed(Container parent,String name){
        for(Component c:parent.getComponents()){if(c instanceof JComponent j&&j.getAccessibleContext()!=null&&name.equals(j.getAccessibleContext().getAccessibleName()))return j;if(c instanceof Container box){JComponent found=findNamed(box,name);if(found!=null)return found;}}return null;
    }
    private static JCheckBox findCheckbox(Container parent,String text){
        for(Component c:parent.getComponents()){if(c instanceof JCheckBox b&&b.getText().equals(text))return b;if(c instanceof Container box){JCheckBox found=findCheckbox(box,text);if(found!=null)return found;}}return null;
    }
    private static boolean hasLabel(Container parent,String text,JComponent target){
        for(Component c:parent.getComponents()){if(c instanceof JLabel l&&l.getText().equals(text)&&l.getLabelFor()==target)return true;if(c instanceof Container box&&hasLabel(box,text,target))return true;}return false;
    }
    private static JButton findButton(Container parent,String text){
        for(Component c:parent.getComponents()){if(c instanceof JButton b && b.getText().equals(text))return b;if(c instanceof Container box){JButton found=findButton(box,text);if(found!=null)return found;}}return null;
    }
    private static void awaitIdle(MainFrame frame)throws Exception{
        CountDownLatch latch=new CountDownLatch(1);AtomicInteger stable=new AtomicInteger();
        SwingUtilities.invokeLater(()->{javax.swing.Timer timer=new javax.swing.Timer(100,e->{if(!frame.isBusy()){if(stable.incrementAndGet()>=3){((javax.swing.Timer)e.getSource()).stop();latch.countDown();}}else stable.set(0);});timer.start();});
        assertTrue(latch.await(30,TimeUnit.SECONDS),"GUI database operation timed out");
    }
    private static void capture(Window window,String name)throws Exception{
        Path folder=Path.of("docs/screenshots");Files.createDirectories(folder);
        Robot robot=new Robot();robot.waitForIdle();SwingUtilities.invokeAndWait(()->{window.validate();window.repaint();});robot.waitForIdle();Toolkit.getDefaultToolkit().sync();
        ImageIO.write(robot.createScreenCapture(new Rectangle(window.getLocationOnScreen(),window.getSize())),"png",folder.resolve(name).toFile());
    }
}