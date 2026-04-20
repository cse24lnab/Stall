package org.example.stall_manage.mapper;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;

import java.util.List;

public interface StallMapper {

    List<Stall> findAll();

    Stall find(String name);

    void add(Stall stall);
}
