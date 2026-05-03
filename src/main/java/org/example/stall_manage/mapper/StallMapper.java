package org.example.stall_manage.mapper;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;

import java.util.List;

public interface StallMapper {
    //todo 其他字段没有写动态sql
    List<Stall> find(Stall stall);

    void add(Stall stall);
}
