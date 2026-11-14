package edu.limkokwing.marketflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/** Domain rules are independent of Swing and JDBC. Money is never a double. */
public final class Domain {
    private Domain() {}
    public static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("9999999.99")) > 0)
            throw new IllegalArgumentException("Money must be between 0 and 9,999,999.99.");
        return value.setScale(2, RoundingMode.HALF_UP);
    }
    public record Product(long id, String sku, String name, String category, BigDecimal price,
                          int stock, int reorderLevel, boolean active) {
        public Product {
            sku = required(sku, "SKU", 40).toUpperCase(Locale.ROOT);
            name = required(name, "Name", 120);
            category = required(category, "Category", 60);
            price = money(price);
            if (stock < 0 || stock > 1_000_000 || reorderLevel < 0 || reorderLevel > 1_000_000)
                throw new IllegalArgumentException("Stock and reorder level must be 0–1,000,000.");
        }
        @Override public String toString() { return sku + " — " + name; }
    }
    private static String required(String text, String field, int max) {
        if (text == null || text.trim().isEmpty() || text.trim().length() > max)
            throw new IllegalArgumentException(field + " is required (maximum " + max + " characters).");
        return text.trim();
    }
    public record Line(Product product, int quantity) {
        public Line {
            if (product == null || !product.active()) throw new IllegalArgumentException("Product is unavailable.");
            if (quantity < 1 || quantity > product.stock()) throw new IllegalArgumentException("Quantity exceeds available stock.");
        }
        public BigDecimal total() { return product.price().multiply(BigDecimal.valueOf(quantity)); }
    }
    public static final class Cart {
        private final Map<Long, Line> lines = new LinkedHashMap<>();
        public void add(Product product, int quantity) {
            if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive.");
            Line old = lines.get(product.id());
            lines.put(product.id(), new Line(product, Math.addExact(quantity, old == null ? 0 : old.quantity())));
        }
        public void remove(long id) { lines.remove(id); }
        public void clear() { lines.clear(); }
        public List<Line> lines() { return List.copyOf(lines.values()); }
        public BigDecimal subtotal() { return lines.values().stream().map(Line::total).reduce(new BigDecimal("0.00"), BigDecimal::add); }
        public BigDecimal total(int discountPercent) {
            if (discountPercent < 0 || discountPercent > 50) throw new IllegalArgumentException("Discount must be 0–50%.");
            return subtotal().multiply(BigDecimal.valueOf(100L - discountPercent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }
    public enum Payment { CASH, CARD }
    public record Sale(long id, LocalDateTime createdAt, BigDecimal subtotal, int discountPercent,
                       BigDecimal total, Payment payment, BigDecimal tendered, BigDecimal change) {}
    public record Receipt(Sale sale, List<Line> lines) {
        public String text() {
            StringBuilder text = new StringBuilder("MARKETFLOW • RETAIL RECEIPT\nSLE — Sierra Leone Leone\n");
            text.append("Receipt #").append(sale.id()).append("\n").append(sale.createdAt()).append("\n------------------------------------------\n");
            for (Line line : lines) text.append(String.format("%-24s %3d x %8s = %s%n", line.product().name(), line.quantity(), line.product().price(), line.total()));
            text.append("------------------------------------------\nSubtotal: SLE ").append(sale.subtotal())
                .append("\nDiscount: ").append(sale.discountPercent()).append("%\nTOTAL: SLE ").append(sale.total())
                .append("\nPayment: ").append(sale.payment()).append("\nTendered: SLE ").append(sale.tendered())
                .append("\nChange: SLE ").append(sale.change()).append("\n\nThank you for shopping locally.\n");
            return text.toString();
        }
    }
}