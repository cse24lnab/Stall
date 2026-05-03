package org.example.stall_manage.service;

import org.example.stall_manage.pojo.Dish;

import java.util.List;

public interface DishService {
    List<Dish> find(Dish dish);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的菜的名字还是用不了，算是个bug
    void add (Dish dish);

    /**
     * 目前的设计是根据id删和根据stallId删写两个接口
     * 删除逻辑和stall的一样
     * @see StallService
     */
    void deleteById(List<Integer> ids);
}
