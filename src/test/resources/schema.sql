DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS stall;
DROP TABLE IF EXISTS `user`;

CREATE TABLE stall (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    current_status INT NOT NULL DEFAULT 0,
    noon_location VARCHAR(100),
    evening_location VARCHAR(100),
    noon_start_time TIME,
    noon_end_time TIME,
    evening_start_time TIME,
    evening_end_time TIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_stall_name UNIQUE (name)
);

CREATE TABLE dish (
    id INT AUTO_INCREMENT PRIMARY KEY,
    stall_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_sold_out INT NOT NULL DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_dish_name UNIQUE (name),
    CONSTRAINT fk_dish_stall FOREIGN KEY (stall_id) REFERENCES stall (id)
);

CREATE TABLE `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(25) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(25) NOT NULL,
    phone VARCHAR(11) UNIQUE,
    avatar_file_id INT,
    avatar_url VARCHAR(255),
    role TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0
);
