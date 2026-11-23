package edu.limkokwing.marketflow;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import static edu.limkokwing.marketflow.Domain.*;

/** Custom components share behavior through inheritance and override cell rendering polymorphically. */
public final class Tables {
    private Tables() {}
    public abstract static class TypedModel<T> extends AbstractTableModel {
        protected List<T> rows = List.of();
        private final String[] columns;
        protected TypedModel(String... columns) { this.columns = columns; }
        public void setRows(List<T> rows) { this.rows = List.copyOf(rows); fireTableDataChanged(); }
        public T row(int index) { return rows.get(index); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public boolean isCellEditable(int row,int column) { return false; }
    }
    public static final class Products extends TypedModel<Product> {
        public Products() { super("SKU","Product","Category","Price (SLE)","Stock","Reorder","Status"); }
        @Override public Object getValueAt(int r,int c) {
            Product p=row(r); return switch(c) { case 0 -> p.sku(); case 1 -> p.name(); case 2 -> p.category(); case 3 -> p.price(); case 4 -> p.stock(); case 5 -> p.reorderLevel(); default -> !p.active()?"Archived":p.stock()<=p.reorderLevel()?"Low stock":"Available"; };
        }
        @Override public Class<?> getColumnClass(int c) { return c==3?java.math.BigDecimal.class:c==4||c==5?Integer.class:String.class; }
    }
    public static final class CartLines extends TypedModel<Line> {
        public CartLines() { super("Product","Quantity","Unit (SLE)","Total (SLE)"); }
        @Override public Object getValueAt(int r,int c) { Line l=row(r); return switch(c) {case 0 -> l.product().name(); case 1 -> l.quantity(); case 2 -> l.product().price(); default -> l.total(); }; }
        @Override public Class<?> getColumnClass(int c) { return c==0?String.class:c==1?Integer.class:java.math.BigDecimal.class; }
    }
    public static final class Sales extends TypedModel<Sale> {
        public Sales() { super("Receipt #","Date / time","Subtotal (SLE)","Discount %","Total (SLE)","Payment"); }
        @Override public Object getValueAt(int r,int c) { Sale s=row(r); return switch(c) {case 0 -> s.id(); case 1 -> s.createdAt(); case 2 -> s.subtotal(); case 3 -> s.discountPercent(); case 4 -> s.total(); default -> s.payment(); }; }
        @Override public Class<?> getColumnClass(int c) { return switch(c) {case 0 -> Long.class;case 1 -> java.time.LocalDateTime.class;case 2,4 -> java.math.BigDecimal.class;case 3 -> Integer.class;default -> Payment.class;}; }
    }
}