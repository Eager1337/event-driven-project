USE marketflow;
-- Synthetic demonstration products, not real retailer data. Never reset existing stock.
INSERT IGNORE INTO products(sku,name,category,price,stock,reorder_level) VALUES
('RICE-5KG','Local rice 5 kg','Food',185.00,48,10),
('OIL-1L','Vegetable oil 1 L','Food',65.00,35,8),
('SUGAR-1KG','Sugar 1 kg','Food',32.50,40,10),
('BREAD-001','Fresh bread loaf','Food',18.00,16,5),
('WATER-1L','Drinking water 1 L','Drinks',12.00,90,20),
('JUICE-500','Mango juice 500 ml','Drinks',24.00,30,8),
('SOAP-001','Laundry soap','Household',15.50,8,10),
('TISSUE-004','Tissue pack','Household',28.00,22,5),
('BOOK-A5','A5 exercise book','Stationery',20.00,50,10),
('PEN-BLUE','Blue ballpoint pen','Stationery',5.00,120,25),
('TEA-025','Tea bags 25 pack','Food',42.00,4,6),
('MILK-400','Powdered milk 400 g','Food',88.00,18,5);