package edu.limkokwing.marketflow;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import static edu.limkokwing.marketflow.Domain.*;

/** JDBC repository and transactional checkout boundary. No SQL runs on the event-dispatch thread. */
public final class Store {
    private final Database db;
    public Store(Database db) { this.db = db; }
    private Product product(ResultSet r) throws SQLException {
        return new Product(r.getLong("id"), r.getString("sku"), r.getString("name"), r.getString("category"),
            r.getBigDecimal("price"), r.getInt("stock"), r.getInt("reorder_level"), r.getBoolean("active"));
    }
    public List<Product> products(boolean includeArchived) throws SQLException {
        List<Product> results = new ArrayList<>();
        try (Connection c = db.open(); PreparedStatement s = c.prepareStatement("SELECT * FROM products WHERE active=TRUE OR ? ORDER BY name")) {
            s.setBoolean(1, includeArchived); try (ResultSet r = s.executeQuery()) { while (r.next()) results.add(product(r)); }
        }
        return results;
    }
    public void save(Product p) throws SQLException {
        save(p,null);
    }
    public void save(Product p, Product original) throws SQLException {
        String sql = p.id() == 0 ? "INSERT INTO products(sku,name,category,price,stock,reorder_level,active) VALUES(?,?,?,?,?,?,?)" :
            "UPDATE products SET sku=?,name=?,category=?,price=?,stock=?,reorder_level=?,active=? WHERE id=?";
        if (p.id() != 0 && original != null) sql += " AND sku=? AND name=? AND category=? AND price=? AND stock=? AND reorder_level=? AND active=?";
        try (Connection c = db.open(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1,p.sku()); s.setString(2,p.name()); s.setString(3,p.category()); s.setBigDecimal(4,p.price());
            s.setInt(5,p.stock()); s.setInt(6,p.reorderLevel()); s.setBoolean(7,p.active());
            if (p.id() != 0) s.setLong(8,p.id());
            if (p.id() != 0 && original != null) {
                s.setString(9,original.sku()); s.setString(10,original.name()); s.setString(11,original.category());
                s.setBigDecimal(12,original.price()); s.setInt(13,original.stock()); s.setInt(14,original.reorderLevel()); s.setBoolean(15,original.active());
            }
            if (s.executeUpdate() != 1) throw new SQLException("Product changed or no longer exists; refresh inventory before editing.");
        }
    }
    public void archive(long id) throws SQLException {
        try (Connection c = db.open(); PreparedStatement s = c.prepareStatement("UPDATE products SET active=FALSE WHERE id=?")) {
            s.setLong(1,id); if (s.executeUpdate() != 1) throw new SQLException("Product no longer exists.");
        }
    }
    public Receipt checkout(List<Line> requested, int discount, Payment payment, BigDecimal tendered) throws SQLException {
        if (requested.isEmpty()) throw new IllegalArgumentException("Add at least one product before checkout.");
        if (discount < 0 || discount > 50) throw new IllegalArgumentException("Discount must be 0–50%.");
        Objects.requireNonNull(payment, "Choose a payment method.");
        tendered = money(tendered);
        try (Connection c = db.open()) {
            c.setAutoCommit(false);
            try {
                List<Line> verified = new ArrayList<>(); Set<Long> ids = new HashSet<>();
                // Deterministic row-lock ordering avoids the most common multi-terminal deadlock.
                for (Line line : requested.stream().sorted(Comparator.comparingLong(l -> l.product().id())).toList()) {
                    if (!ids.add(line.product().id())) throw new IllegalArgumentException("Duplicate product in checkout.");
                    try (PreparedStatement s = c.prepareStatement("SELECT * FROM products WHERE id=? FOR UPDATE")) {
                        s.setLong(1,line.product().id()); try (ResultSet r = s.executeQuery()) {
                            if (!r.next()) throw new IllegalArgumentException("Product was removed; clear cart and refresh.");
                            Product current = product(r);
                            if (!current.active() || current.stock() < line.quantity()) throw new IllegalArgumentException("Insufficient stock for " + current.name() + ". Clear cart and refresh.");
                            if (current.price().compareTo(line.product().price()) != 0) throw new IllegalArgumentException("Price changed for " + current.name() + ". Clear cart and refresh.");
                            verified.add(new Line(current, line.quantity()));
                        }
                    }
                }
                Cart cart = new Cart(); for (Line l : verified) cart.add(l.product(),l.quantity());
                BigDecimal total = cart.total(discount);
                if (total.compareTo(new BigDecimal("9999999.99")) > 0) throw new IllegalArgumentException("Sale total exceeds supported maximum.");
                if (payment == Payment.CARD) tendered = total;
                if (tendered.compareTo(total) < 0) throw new IllegalArgumentException("Cash tendered is less than the total.");
                long id;
                try (PreparedStatement s = c.prepareStatement("INSERT INTO sales(subtotal,discount_percent,total,payment,tendered,change_due) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setBigDecimal(1,cart.subtotal()); s.setInt(2,discount); s.setBigDecimal(3,total); s.setString(4,payment.name());
                    s.setBigDecimal(5,tendered); s.setBigDecimal(6,tendered.subtract(total)); s.executeUpdate();
                    try (ResultSet r = s.getGeneratedKeys()) { r.next(); id=r.getLong(1); }
                }
                for (Line l : verified) {
                    try (PreparedStatement s = c.prepareStatement("INSERT INTO sale_items(sale_id,product_id,sku,product_name,unit_price,quantity) VALUES(?,?,?,?,?,?)")) {
                        s.setLong(1,id); s.setLong(2,l.product().id()); s.setString(3,l.product().sku()); s.setString(4,l.product().name());
                        s.setBigDecimal(5,l.product().price()); s.setInt(6,l.quantity()); s.executeUpdate();
                    }
                    try (PreparedStatement s = c.prepareStatement("UPDATE products SET stock=stock-? WHERE id=?")) {
                        s.setInt(1,l.quantity()); s.setLong(2,l.product().id()); s.executeUpdate();
                    }
                }
                Sale sale = sale(c,id); c.commit(); return new Receipt(sale, List.copyOf(verified));
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    private Sale readSale(ResultSet r) throws SQLException {
        return new Sale(r.getLong("id"),r.getTimestamp("created_at").toLocalDateTime(),r.getBigDecimal("subtotal"),
            r.getInt("discount_percent"),r.getBigDecimal("total"),Payment.valueOf(r.getString("payment")),r.getBigDecimal("tendered"),r.getBigDecimal("change_due"));
    }
    private Sale sale(Connection c,long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT * FROM sales WHERE id=?")) {
            s.setLong(1,id); try (ResultSet r = s.executeQuery()) { if (!r.next()) throw new SQLException("Sale not found."); return readSale(r); }
        }
    }
    public List<Sale> sales() throws SQLException {
        return salesPage(0,1000).rows();
    }
    /** Keyset pagination uses the primary-key index instead of increasingly deep OFFSET scans. */
    public record SalesPage(List<Sale> rows, boolean hasOlder) {
        public SalesPage { rows = List.copyOf(rows); }
        public long olderCursor() { return rows.isEmpty() ? 0 : rows.get(rows.size()-1).id(); }
    }
    public SalesPage salesPage(long beforeId, int size) throws SQLException {
        if(beforeId<0 || size<1 || size>1000)throw new IllegalArgumentException("Invalid history cursor or page size (1–1000).");
        List<Sale> results = new ArrayList<>();
        String sql="SELECT * FROM sales"+(beforeId==0?"":" WHERE id < ?")+" ORDER BY id DESC LIMIT ?";
        try (Connection c = db.open(); PreparedStatement s = c.prepareStatement(sql)) {
            int parameter=1;if(beforeId!=0)s.setLong(parameter++,beforeId);s.setInt(parameter,size+1);
            try(ResultSet r=s.executeQuery()){while(r.next())results.add(readSale(r));}
        }
        boolean hasOlder=results.size()>size;
        return new SalesPage(results.subList(0,Math.min(size,results.size())),hasOlder);
    }
    public Receipt receipt(long id) throws SQLException {
        try (Connection c = db.open()) {
            Sale sale = sale(c,id); List<Line> lines = new ArrayList<>();
            try (PreparedStatement s = c.prepareStatement("SELECT * FROM sale_items WHERE sale_id=? ORDER BY id")) {
                s.setLong(1,id); try (ResultSet r = s.executeQuery()) { while(r.next()) {
                    int quantity = r.getInt("quantity");
                    // Snapshot fields preserve historical receipts after inventory edits/archives.
                    Product p = new Product(r.getLong("product_id"),r.getString("sku"),r.getString("product_name"),"Historical",r.getBigDecimal("unit_price"),quantity,0,true);
                    lines.add(new Line(p,quantity));
                } }
            }
            return new Receipt(sale,List.copyOf(lines));
        }
    }
}