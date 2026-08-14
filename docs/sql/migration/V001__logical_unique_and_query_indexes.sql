-- Apply once to a database created from the original canteen.sql.
-- Select the target schema before running this migration.

ALTER TABLE `stall`
    DROP INDEX `index_name`,
    ADD COLUMN `active_name` VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN `is_delete` = 0 THEN `name` ELSE NULL END
    ) STORED COMMENT '仅有效记录保留名称，用于逻辑删除唯一约束',
    ADD UNIQUE INDEX `uk_stall_active_name` (`active_name`),
    ADD INDEX `idx_stall_owner_delete_id` (`owner_user_id`, `is_delete`, `id`);

ALTER TABLE `dish`
    DROP FOREIGN KEY `fk_dish_stall`;

ALTER TABLE `dish`
    DROP INDEX `index_name`,
    DROP INDEX `fk_dish_stall`,
    ADD COLUMN `active_name` VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN `is_delete` = 0 THEN `name` ELSE NULL END
    ) STORED COMMENT '仅有效记录保留名称，用于逻辑删除唯一约束',
    ADD UNIQUE INDEX `uk_dish_stall_active_name` (`stall_id`, `active_name`),
    ADD INDEX `idx_dish_stall_delete_id` (`stall_id`, `is_delete`, `id`);
