package org.lab.stall_manage.mapper;

import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.service.StallService;

import java.util.List;

public interface DishMapper {
    //todo 分页查询和根据价格降序
    //todo 名字模糊查询
    List<Dish> find(Dish dish);

    void add(Dish dish);

    void deleteById(List<Integer> ids);

    /**
     * 目前是用来给stall的删除操作使用，同一事务
     * @see StallService
     */
    void deleteByStallId(List<Integer> stallIds);

    void update(Dish dish);
}
