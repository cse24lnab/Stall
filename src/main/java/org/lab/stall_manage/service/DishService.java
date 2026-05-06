package org.lab.stall_manage.service;

import org.lab.stall_manage.pojo.Dish;

import java.util.List;
import java.util.Optional;

public interface DishService {
    /**
     * 查询菜品
     * @param dish
     * @return
     */
    List<Dish> find(Dish dish);

    /**
     * 根据id查询特定菜品
     */
    Optional<Dish> findById(Integer id);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的菜的名字还是用不了，算是个bug
    /**
     * 增加菜品
     * @param dish
     */
    void add (Dish dish);

    /**
     * 删除逻辑和stall的一样
     * @see StallService
     */
    void deleteById(List<Integer> ids);

    /**
     * 修改菜品，其中stallId不可修改
     * @param dish
     */
    void update(Dish dish);
}
