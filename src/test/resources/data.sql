INSERT INTO stall (
    id, name, current_status, noon_location, evening_location,
    noon_start_time, noon_end_time, evening_start_time, evening_end_time
) VALUES
    (1, '烤冷面', 1, '东区', '西区', '11:00:00', '13:00:00', '17:00:00', '20:00:00'),
    (2, '煎饼', 0, '南区', '北区', '10:00:00', '12:30:00', '16:30:00', '19:30:00');

INSERT INTO dish (id, stall_id, name, price, is_sold_out) VALUES
    (1, 1, '招牌烤冷面', 12.50, 0),
    (2, 1, '豪华烤冷面', 15.00, 1),
    (3, 2, '鸡蛋煎饼', 8.00, 0);
