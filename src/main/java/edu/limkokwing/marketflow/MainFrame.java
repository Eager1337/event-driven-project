package edu.limkokwing.marketflow;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import static edu.limkokwing.marketflow.Domain.*;

/** All UI mutations are on Swing's EDT; database work executes in SwingWorker. */
public final class MainFrame extends JFrame {
    private final Store store;
    private final Cart cart = new Cart();
    final JTabbedPane tabs = new JTabbedPane();
    final Tables.Products productModel = new Tables.Products();
    final Tables.CartLines cartModel = new Tables.CartLines();
    final Tables.Sales salesModel = new Tables.Sales();
    final JTable products = table(productModel,"Product inventory");
    final JTable cartTable = table(cartModel,"Shopping cart");
    final JTable salesTable = table(salesModel,"Sales history");
    final JTextField search = new JTextField(22);
    private final JCheckBox archived = new JCheckBox("Include archived");
    private final JCheckBox lowStock = new JCheckBox("Low stock only");
    private final JComboBox<String> category = new JComboBox<>(new String[]{"All categories"});
    private final JSpinner quantity = new JSpinner(new SpinnerNumberModel(1,1,1_000_000,1));
    final JSpinner discount = new JSpinner(new SpinnerNumberModel(0,0,50,1));
    final JTextField tendered = new JTextField("0.00",9);
    private final JRadioButton cash = new JRadioButton("Cash",true), card = new JRadioButton("Card");
    private final JLabel total = new JLabel("Total: SLE 0.00"), subtotal = new JLabel("Subtotal: SLE 0.00");
    private final JLabel status = new JLabel("Connecting to database…"), overview = new JLabel("Loading inventory…");
    private final JPanel workspace = new JPanel(new BorderLayout(12,12));
    private final TableRowSorter<Tables.Products> sorter = new TableRowSorter<>(productModel);
    private boolean busy;
    private Receipt lastReceipt;

    public MainFrame(Store store) {
        super("MarketFlow | Retail Point of Sale"); this.store=store;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1000,680)); setSize(1240,800); setLocationRelativeTo(null);
        workspace.setBorder(BorderFactory.createEmptyBorder(20,24,14,24));
        JLabel title=new JLabel("MarketFlow  /  Retail workspace"); title.setFont(new Font("SansSerif",Font.BOLD,26));
        JPanel heading=new JPanel(new BorderLayout()); heading.add(title,BorderLayout.NORTH); heading.add(overview,BorderLayout.SOUTH);
        workspace.add(heading,BorderLayout.NORTH);
        tabs.addTab("1  Checkout", checkoutPanel()); tabs.addTab("2  Sales history", historyPanel()); tabs.addTab("3  Help & accessibility",helpPanel());
        workspace.add(tabs,BorderLayout.CENTER); workspace.add(status,BorderLayout.SOUTH); setContentPane(workspace);
        products.setRowSorter(sorter);
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){filter();} public void removeUpdate(DocumentEvent e){filter();} public void changedUpdate(DocumentEvent e){filter();}
        });
        category.addItemListener(e->{if(e.getStateChange()==ItemEvent.SELECTED)filter();});
        lowStock.addItemListener(e->filter()); archived.addActionListener(e->refresh());
        discount.addChangeListener(e->updateCart());
        card.addItemListener(e->tendered.setEnabled(cash.isSelected() && !busy));
        products.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){if(e.getClickCount()==2 && !busy)addSelected();}});
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){
            if(busy){message("Please wait for the current operation to finish.");return;}
            if(cart.lines().isEmpty() || JOptionPane.showConfirmDialog(MainFrame.this,"Discard the current cart and exit?","Exit",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)dispose();
        }});
        shortcut("control F","focus-search",()->{tabs.setSelectedIndex(0);search.requestFocusInWindow();});
        shortcut("F5","refresh",()->{if(!busy)refresh();});
        shortcut("control ENTER","complete-sale",()->{if(!busy)completeSale();});
        shortcut("alt 1","checkout-tab",()->tabs.setSelectedIndex(0)); shortcut("alt 2","history-tab",()->tabs.setSelectedIndex(1));
        accessible(search,"Search products","Filter by product name or SKU; Control F focuses search.");
        accessible(category,"Category filter","Select a product category."); accessible(quantity,"Quantity","Number of units to add.");
        accessible(discount,"Discount percentage","Between zero and fifty percent."); accessible(tendered,"Cash tendered","Amount received from customer in SLE.");
        accessible(status,"Operation status","Reports database and validation outcomes."); accessible(tabs,"Workspace sections","Use Alt 1 and Alt 2 to switch sections.");
        refresh();
    }
    private JPanel checkoutPanel() {
        JPanel catalog=new JPanel(new BorderLayout(8,8));
        JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        JLabel label=new JLabel("Search");label.setLabelFor(search); filters.add(label); filters.add(search); filters.add(category);
        JPanel options=new JPanel(new FlowLayout(FlowLayout.LEFT)); options.add(lowStock);options.add(archived);
        JPanel top=new JPanel(new BorderLayout());top.add(filters,BorderLayout.NORTH);top.add(options,BorderLayout.SOUTH);
        catalog.add(top,BorderLayout.NORTH); catalog.add(new JScrollPane(products),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(new JLabel("Qty"));actions.add(quantity); actions.add(button("Add to cart",KeyEvent.VK_A,this::addSelected));
        actions.add(button("New product",KeyEvent.VK_N,()->editProduct(null)));
        actions.add(button("Edit",KeyEvent.VK_E,()->{Product p=selectedProduct();if(p!=null)editProduct(p);}));
        actions.add(button("Archive",KeyEvent.VK_R,this::archiveSelected));actions.add(button("Refresh",KeyEvent.VK_F, this::refresh));
        catalog.add(actions,BorderLayout.SOUTH);
        JPanel basket=new JPanel(new BorderLayout(8,8));
        JLabel title=new JLabel("CURRENT SALE");title.setFont(title.getFont().deriveFont(Font.BOLD,16f));basket.add(title,BorderLayout.NORTH);
        basket.add(new JScrollPane(cartTable),BorderLayout.CENTER);
        JPanel pay=new JPanel(new GridLayout(0,1,4,8));
        JPanel remove=new JPanel(new FlowLayout(FlowLayout.LEFT));
        remove.add(button("Remove item",KeyEvent.VK_M,()->{int r=cartTable.getSelectedRow();if(r>=0){cart.remove(cartModel.row(cartTable.convertRowIndexToModel(r)).product().id());updateCart();}else message("Select a cart item first.");}));
        remove.add(button("Clear cart",KeyEvent.VK_L,()->{if(cart.lines().isEmpty() || JOptionPane.showConfirmDialog(this,"Clear all cart items?","Clear cart",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){cart.clear();updateCart();}}));
        pay.add(remove);pay.add(subtotal);
        JPanel discountRow=new JPanel(new FlowLayout(FlowLayout.LEFT));discountRow.add(new JLabel("Discount %"));discountRow.add(discount);pay.add(discountRow);
        total.setFont(new Font("SansSerif",Font.BOLD,23));pay.add(total);
        JPanel method=new JPanel(new FlowLayout(FlowLayout.LEFT));ButtonGroup group=new ButtonGroup();group.add(cash);group.add(card);method.add(cash);method.add(card);method.add(new JLabel("Tendered SLE"));method.add(tendered);pay.add(method);
        pay.add(button("Complete sale  /  Ctrl+Enter",KeyEvent.VK_C,this::completeSale));
        basket.add(pay,BorderLayout.SOUTH);
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,catalog,basket);split.setResizeWeight(0.60);split.setDividerLocation(700);
        JPanel panel=new JPanel(new BorderLayout());panel.add(split);return panel;
    }
    private JPanel historyPanel() {
        JPanel panel=new JPanel(new BorderLayout(12,12));
        panel.add(new JLabel("Latest 1,000 sales • Select a receipt to view, save, or print"),BorderLayout.NORTH);
        panel.add(new JScrollPane(salesTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("View receipt",KeyEvent.VK_V,()->{int r=salesTable.getSelectedRow();if(r<0){message("Select a sale first.");return;}long id=salesModel.row(salesTable.convertRowIndexToModel(r)).id();work(()->store.receipt(id),this::showReceipt);}));
        actions.add(button("Export sales CSV",KeyEvent.VK_X,this::exportSales)); actions.add(button("Refresh history",KeyEvent.VK_H,this::refresh));panel.add(actions,BorderLayout.SOUTH);return panel;
    }
    private JPanel helpPanel() {
        JPanel panel=new JPanel(new BorderLayout(12,12));
        JTextArea help=new JTextArea("MARKETFLOW — QUICK GUIDE\n\n1. Search or filter products. Select a row, choose quantity, and Add to cart.\n2. Choose discount and Cash or Card. For cash, enter the amount received.\n3. Complete sale. Stock and receipt are saved in one database transaction.\n4. Use Sales history to re-open old receipts or export CSV.\n5. New product / Edit manage inventory; Archive preserves past receipts.\n\nKEYBOARD\nCtrl+F: Search    F5: Refresh    Ctrl+Enter: Checkout\nAlt+1: Checkout tab    Alt+2: Sales history\nTab / Shift+Tab: Move focus    Space: Toggle checkbox/radio button\nUse button mnemonics with Alt and the underlined letter (platform dependent).\n\nACCESSIBILITY & OPENNESS\nNamed Swing accessible contexts, visible focus, scalable fonts, high contrast,\nkeyboard-operable tables and forms. Status messages do not rely on color.\nNo customer personal data is collected. Source is MIT licensed.\n\nThis is an academic, single-operator POS, not a production payment processor.\nCard is a recorded payment method only; no card data is collected or charged.\nDiscount is a demonstration policy; taxes are not calculated.\nDatabase credentials stay on your computer. See docs/ for details.");
        help.setEditable(false);help.setLineWrap(true);help.setWrapStyleWord(true);help.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));accessible(help,"User guide","Application instructions and accessibility shortcuts.");
        panel.add(new JScrollPane(help),BorderLayout.CENTER);
        JPanel options=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox contrast=new JCheckBox("High contrast");contrast.addItemListener(e->{Color bg=contrast.isSelected()?Color.BLACK:new Color(245,247,250),fg=contrast.isSelected()?Color.WHITE:new Color(22,38,53);recolor(workspace,bg,fg);});
        JComboBox<String> scale=new JComboBox<>(new String[]{"Standard text","Large text","Extra large text"});scale.addActionListener(e->{resizeFonts(workspace,scale.getSelectedIndex()==0?14:scale.getSelectedIndex()==1?17:20);revalidate();});
        accessible(scale,"Text size","Choose standard, large or extra large text.");options.add(contrast);options.add(scale);panel.add(options,BorderLayout.SOUTH);return panel;
    }
    private void filter() {
        String term=search.getText().trim().toLowerCase(java.util.Locale.ROOT);Object selected=category.getSelectedItem();
        sorter.setRowFilter(new RowFilter<>(){public boolean include(Entry<? extends Tables.Products,? extends Integer> entry){
            Product p=productModel.row(entry.getIdentifier());return (p.sku()+" "+p.name()).toLowerCase(java.util.Locale.ROOT).contains(term)
                && (selected==null || selected.equals("All categories") || selected.equals(p.category())) && (!lowStock.isSelected() || p.stock()<=p.reorderLevel());
        }});
    }
    private Product selectedProduct() {int r=products.getSelectedRow();if(r<0){message("Select a product first.");return null;}return productModel.row(products.convertRowIndexToModel(r));}
    void addSelected() { try {quantity.commitEdit();Product p=selectedProduct();if(p!=null){cart.add(p,(int)quantity.getValue());updateCart();status.setText("Added "+p.name()+" to cart.");}}catch(Exception e){error(e);} }
    private void updateCart() {cartModel.setRows(cart.lines());subtotal.setText("Subtotal: SLE "+cart.subtotal());total.setText("Total: SLE "+cart.total((int)discount.getValue()));}
    private void archiveSelected() {Product p=selectedProduct();if(p!=null && JOptionPane.showConfirmDialog(this,"Archive "+p.name()+"? Historical receipts remain available.","Archive product",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)work(()->{store.archive(p.id());return true;},v->refresh());}
    private void editProduct(Product p) {
        JTextField sku=new JTextField(p==null?"":p.sku()), name=new JTextField(p==null?"":p.name()), cat=new JTextField(p==null?"General":p.category()), price=new JTextField(p==null?"0.00":p.price().toPlainString());
        JSpinner stock=new JSpinner(new SpinnerNumberModel(p==null?0:p.stock(),0,1_000_000,1)), reorder=new JSpinner(new SpinnerNumberModel(p==null?5:p.reorderLevel(),0,1_000_000,1));
        JCheckBox active=new JCheckBox("Active / available for sale",p==null||p.active());
        JPanel form=new JPanel(new GridLayout(0,2,12,12));
        addField(form,"SKU",sku);addField(form,"Product name",name);addField(form,"Category",cat);addField(form,"Price (SLE)",price);addField(form,"Stock units",stock);addField(form,"Reorder level",reorder);form.add(new JLabel("Availability"));form.add(active);
        while(JOptionPane.showConfirmDialog(this,form,p==null?"New product":"Edit product",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            try {stock.commitEdit();reorder.commitEdit();Product value=new Product(p==null?0:p.id(),sku.getText(),name.getText(),cat.getText(),new BigDecimal(price.getText().trim()),(int)stock.getValue(),(int)reorder.getValue(),active.isSelected());
                work(()->{store.save(value,p);return true;},v->refresh());return;
            }catch(Exception e){error(e);}
        }
    }
    void completeSale() {
        try {discount.commitEdit();if(cart.lines().isEmpty())throw new IllegalArgumentException("Add at least one product before checkout.");
            Payment payment=cash.isSelected()?Payment.CASH:Payment.CARD;
            BigDecimal amount=payment==Payment.CASH?money(new BigDecimal(tendered.getText().trim())):cart.total((int)discount.getValue());
            if(amount.compareTo(cart.total((int)discount.getValue()))<0)throw new IllegalArgumentException("Cash tendered is less than the total.");
            if(JOptionPane.showConfirmDialog(this,"Save this sale for SLE "+cart.total((int)discount.getValue())+" using "+payment+"?","Confirm checkout",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            List<Line> snapshot=cart.lines();int percent=(int)discount.getValue();
            work(()->store.checkout(snapshot,percent,payment,amount),receipt->{lastReceipt=receipt;cart.clear();discount.setValue(0);tendered.setText("0.00");updateCart();refresh();showReceipt(receipt);});
        }catch(Exception e){error(e);}
    }
    private void showReceipt(Receipt receipt) {
        JTextArea text=new JTextArea(receipt.text(),22,65);text.setFont(new Font("Monospaced",Font.PLAIN,13));text.setEditable(false);text.setCaretPosition(0);accessible(text,"Receipt details","Persisted sale items, totals and change.");
        JDialog dialog=new JDialog(this,"Receipt #"+receipt.sale().id(),false);dialog.setLayout(new BorderLayout(8,8));dialog.add(new JScrollPane(text));
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(button("Save TXT",KeyEvent.VK_S,()->saveText(receipt.text(),"receipt-"+receipt.sale().id()+".txt")));
        actions.add(button("Print",KeyEvent.VK_P,()->{try{text.print();}catch(Exception e){error(e);}}));
        actions.add(button("Close",KeyEvent.VK_O,dialog::dispose));dialog.add(actions,BorderLayout.SOUTH);dialog.pack();dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    private void exportSales() {
        StringBuilder csv=new StringBuilder("Receipt,Date,Subtotal,DiscountPercent,Total,Payment,Tendered,Change\r\n");
        for(int i=0;i<salesModel.getRowCount();i++){Sale s=salesModel.row(i);csv.append(s.id()).append(',').append(s.createdAt()).append(',').append(s.subtotal()).append(',').append(s.discountPercent()).append(',').append(s.total()).append(',').append(s.payment()).append(',').append(s.tendered()).append(',').append(s.change()).append("\r\n");}
        saveText(csv.toString(),"marketflow-sales.csv");
    }
    private void saveText(String text,String name) {
        JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File(name));
        if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){java.nio.file.Path path=chooser.getSelectedFile().toPath();
            if(Files.exists(path) && JOptionPane.showConfirmDialog(this,"Replace existing file?","Overwrite",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            work(()->{Files.writeString(path,text,StandardCharsets.UTF_8);return true;},v->status.setText("Saved "+path.getFileName()));
        }
    }
    private record Snapshot(List<Product> products,List<Sale> sales) {}
    public void refresh() {
        if(busy)return;boolean include=archived.isSelected();Object previous=category.getSelectedItem();
        work(()->new Snapshot(store.products(include),store.sales()),data->{productModel.setRows(data.products());salesModel.setRows(data.sales());
            category.removeAllItems();category.addItem("All categories");data.products().stream().map(Product::category).distinct().sorted().forEach(category::addItem);
            category.setSelectedItem(previous);if(category.getSelectedIndex()<0)category.setSelectedIndex(0);filter();
            long active=data.products().stream().filter(Product::active).count(),low=data.products().stream().filter(p->p.active()&&p.stock()<=p.reorderLevel()).count();
            overview.setText(active+" active products  •  "+low+" need restocking  •  SLE currency  •  MySQL persistence");
            status.setText("Database connected. Inventory and latest sales loaded.");});
    }
    private <T> void work(Callable<T> job,Consumer<T> success) {
        if(busy)return;busy=true;enableTree(workspace,false);status.setText("Working… Please wait.");
        new SwingWorker<T,Void>(){protected T doInBackground()throws Exception{return job.call();}
            protected void done(){busy=false;enableTree(workspace,true);tendered.setEnabled(cash.isSelected());try{success.accept(get());}catch(Exception e){Throwable cause=e.getCause()==null?e:e.getCause();error(cause);}}
        }.execute();
    }
    private void error(Throwable e) {
        String text=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
        if(e instanceof java.sql.SQLIntegrityConstraintViolationException)text="SKU already exists or a database constraint was violated. Use a unique SKU and valid values.";
        else if(e instanceof java.sql.SQLException)text="Database operation failed. Check Docker/database, config.properties and README. "+text;
        status.setText("Error: "+text);JOptionPane.showMessageDialog(this,text,"Operation not completed",JOptionPane.ERROR_MESSAGE);
    }
    private void message(String text){status.setText(text);JOptionPane.showMessageDialog(this,text,"MarketFlow",JOptionPane.INFORMATION_MESSAGE);}
    private JButton button(String text,int mnemonic,Runnable action){JButton button=new JButton(text);button.setMnemonic(mnemonic);button.addActionListener(e->{if(!busy)action.run();});accessible(button,text,"Activate "+text);return button;}
    private void shortcut(String key,String name,Runnable action){getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key),name);getRootPane().getActionMap().put(name,new AbstractAction(){public void actionPerformed(ActionEvent e){action.run();}});}
    private static JTable table(javax.swing.table.TableModel model,String name){JTable table=new JTable(model);table.setRowHeight(34);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setFillsViewportHeight(true);table.getTableHeader().setReorderingAllowed(false);accessible(table,name,"Use arrow keys to select a row.");return table;}
    private static void accessible(JComponent c,String name,String description){c.getAccessibleContext().setAccessibleName(name);c.getAccessibleContext().setAccessibleDescription(description);}
    private static void addField(JPanel panel,String name,JComponent field){JLabel label=new JLabel(name);label.setLabelFor(field);accessible(field,name,"Enter "+name);panel.add(label);panel.add(field);}
    private static void enableTree(Component c,boolean enabled){c.setEnabled(enabled);if(c instanceof Container box)for(Component child:box.getComponents())enableTree(child,enabled);}
    private static void recolor(Component c,Color bg,Color fg){c.setBackground(bg);c.setForeground(fg);if(c instanceof JTable t){t.getTableHeader().setBackground(bg);t.getTableHeader().setForeground(fg);}if(c instanceof Container box)for(Component child:box.getComponents())recolor(child,bg,fg);}
    private static void resizeFonts(Component c,int size){if(c.getFont()!=null)c.setFont(c.getFont().deriveFont((float)size));if(c instanceof JTable t){t.setRowHeight(size+18);t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont((float)size));}if(c instanceof Container box)for(Component child:box.getComponents())resizeFonts(child,size);}
    boolean isBusy(){return busy;} Receipt lastReceipt(){return lastReceipt;}
}