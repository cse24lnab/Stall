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
    owner_user_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    active_name VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN is_delete = 0 THEN name ELSE NULL END
    ),
    CONSTRAINT uk_stall_active_name UNIQUE (active_name)
);

CREATE INDEX idx_stall_owner_delete_id
    ON stall(owner_user_id, is_delete, id);

CREATE TABLE dish (
    id INT AUTO_INCREMENT PRIMARY KEY,
    stall_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_sold_out INT NOT NULL DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    active_name VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN is_delete = 0 THEN name ELSE NULL END
    ),
    CONSTRAINT uk_dish_stall_active_name UNIQUE (stall_id, active_name)
);

CREATE INDEX idx_dish_stall_delete_id
    ON dish(stall_id, is_delete, id);

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
