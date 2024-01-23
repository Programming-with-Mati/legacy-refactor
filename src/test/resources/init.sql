CREATE SCHEMA IF NOT EXISTS shop;
USE shop;

CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment VARCHAR(255),
    customer_email VARCHAR(255),
    state VARCHAR(255)
);

CREATE TABLE item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock INT
);

CREATE TABLE order_item (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      quantity INT,
                      item_id BIGINT,
                      order_id BIGINT
);
-- Insert data into the 'order' table
INSERT INTO `order` (payment, customer_email, state) VALUES
                                                         ('PAID', 'customer1@example.com', 'CREATED'),
                                                         ('PAID', 'customer2@example.com', 'DISPATCHED'),
                                                         ('PAID', 'customer3@example.com', 'DELIVERED'),
                                                         ('PAYMENT_IN_PROGRESS', 'customer4@example.com', 'CREATED');

-- Insert data into the 'item' table
INSERT INTO item (stock) VALUES
                             (100),
                             (50),
                             (200);

-- Insert data into the 'order_item' table
INSERT INTO order_item (quantity, item_id, order_id) VALUES
                                                        (2, 1, 1),
                                                        (1, 2, 1),
                                                        (3, 3, 2),
                                                        (2, 1, 3),
                                                        (2, 3, 4);
