package org.example.stall_manage.mapper;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;

import java.util.List;

public interface DishMapper {
    //todo 分页查询和根据价格降序
    List<Dish> find(Dish dish);

    void add(Dish dish);
}
