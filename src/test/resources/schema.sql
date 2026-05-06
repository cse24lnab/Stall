DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS stall;

CREATE TABLE stall (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    current_status INT DEFAULT 0,
    noon_location VARCHAR(100),
    evening_location VARCHAR(100),
    noon_start_time TIME,
    noon_end_time TIME,
    evening_start_time TIME,
    evening_end_time TIME,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE dish (
    id INT AUTO_INCREMENT PRIMARY KEY,
    stall_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_sold_out INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_dish_stall FOREIGN KEY (stall_id) REFERENCES stall (id)
);
