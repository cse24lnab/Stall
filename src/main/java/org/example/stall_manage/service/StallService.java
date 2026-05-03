package org.example.stall_manage.service;

import org.example.stall_manage.pojo.Stall;

import java.util.List;
import java.util.Optional;

public interface StallService {
    List<Stall> find(Stall stall);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的摊位的名字还是用不了，算是个bug
    void add(Stall stall);
}
