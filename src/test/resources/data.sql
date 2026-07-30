INSERT INTO stall (
    id, name, current_status, noon_location, evening_location,
    noon_start_time, noon_end_time, evening_start_time, evening_end_time,
    owner_user_id
) VALUES
    (1, '烤冷面', 1, '东区', '西区', '11:00:00', '13:00:00', '17:00:00', '20:00:00', 2),
    (2, '煎饼', 0, '南区', '北区', '10:00:00', '12:30:00', '16:30:00', '19:30:00', 4);

INSERT INTO dish (id, stall_id, name, price, is_sold_out) VALUES
    (1, 1, '招牌烤冷面', 12.50, 0),
    (2, 1, '豪华烤冷面', 15.00, 1),
    (3, 2, '鸡蛋煎饼', 8.00, 0);

INSERT INTO `user`
(`username`, `password_hash`, `nickname`, `role`)
VALUES
    ('user_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '普通用户演示账号',  0 ),
    ('merchant_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '商家演示账号' ,1),
    ('admin_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '管理员演示账号',  2 ),
    ('merchant_other', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '其他商家演示账号', 1);
