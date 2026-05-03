package org.example.stall_manage.service;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;

import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Optional;

public interface DishService {
    List<Dish> find(Dish dish);


    void add (Dish dish);
}
