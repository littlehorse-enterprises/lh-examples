-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

INSERT INTO products (productId, name, description, price, cost, quantity, category) VALUES (1, 'Laptop', 'High performance laptop with SSD', 2000, 1200, 20, 'Electronics');
INSERT INTO products (productId, name, description, price, cost, quantity, category) VALUES (2, 'Smartphone', 'Latest model with high resolution camera', 1000, 600, 3, 'Electronics');
INSERT INTO products (productId, name, description, price, cost, quantity, category) VALUES (3, 'Taco', 'Mexican food', 2, 1, 10, 'Food');
INSERT INTO products (productId, name, description, price, cost, quantity, category) VALUES (4, 'Encebollado', 'Ecuadorian food', 2.50, 1, 10, 'Food');
INSERT INTO products (productId, name, description, price, cost, quantity, category) VALUES (5, 'Cheeseburgers', 'USA food', 5, 3, 10, 'Food');
INSERT INTO products (productId, name, description, price, cost, quantity, category) VALUES (6, 'Cachapa', 'Venezuelan food', 5, 3, 10, 'Food');