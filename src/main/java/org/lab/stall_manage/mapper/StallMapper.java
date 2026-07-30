package org.lab.stall_manage.mapper;

import org.lab.stall_manage.pojo.Stall;

import java.util.List;

public interface StallMapper {
    //todo 其他字段没有写动态sql
    //todo 分页查询和根据名字模糊查询
    List<Stall> find(Stall stall);

    Stall findById(Integer id);

    int add(Stall stall);

    int delete(List<Integer> ids);

    int update(Stall stall);
}
