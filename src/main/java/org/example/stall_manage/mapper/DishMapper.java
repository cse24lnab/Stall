package org.example.stall_manage.mapper;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;

import java.util.List;

public interface DishMapper {
    List<Dish> findAll(Integer stallId);

    Dish find(Integer stallId,String name);

    void add(Dish dish);
}
