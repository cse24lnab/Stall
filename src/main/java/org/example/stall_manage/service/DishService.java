package org.example.stall_manage.service;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;

import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Optional;

public interface DishService {
    List<Dish> find(Dish dish);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的菜的名字还是用不了，算是个bug
    void add (Dish dish);
}
