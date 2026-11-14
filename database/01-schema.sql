-- MySQL 8.4+; Docker creates the database and restricted application user.
USE marketflow;
CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  category VARCHAR(60) NOT NULL,
  price DECIMAL(12,2) NOT NULL CHECK(price >= 0 AND price <= 9999999.99),
  stock INT NOT NULL CHECK(stock >= 0 AND stock <= 1000000),
  reorder_level INT NOT NULL DEFAULT 5 CHECK(reorder_level >= 0 AND reorder_level <= 1000000),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  INDEX ix_products_category_active(category,active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS sales (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(16,2) NOT NULL CHECK(subtotal >= 0),
  discount_percent INT NOT NULL CHECK(discount_percent BETWEEN 0 AND 50),
  total DECIMAL(12,2) NOT NULL CHECK(total >= 0 AND total <= 9999999.99),
  payment VARCHAR(10) NOT NULL CHECK(payment IN ('CASH','CARD')),
  tendered DECIMAL(12,2) NOT NULL,
  change_due DECIMAL(12,2) NOT NULL CHECK(change_due >= 0),
  CHECK(tendered >= total),
  INDEX ix_sales_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS sale_items (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sale_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  sku VARCHAR(40) NOT NULL,
  product_name VARCHAR(120) NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL CHECK(unit_price >= 0),
  quantity INT NOT NULL CHECK(quantity > 0),
  UNIQUE KEY uq_sale_product(sale_id,product_id),
  FOREIGN KEY (sale_id) REFERENCES sales(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;