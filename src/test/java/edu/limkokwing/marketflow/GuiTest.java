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