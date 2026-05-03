package org.example.stall_manage.service;

import org.example.stall_manage.pojo.Stall;

import java.util.List;
import java.util.Optional;

public interface StallService {
    /**
     * 根据输入的id或者name查找摊位
     * @param stall
     * @return List<Stall>
     */
    List<Stall> find(Stall stall);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的摊位的名字还是用不了，算是个bug

    /**
     * 增加摊位
     * @param stall
     */
    void add(Stall stall);

    /**
     * 如果is_delete已经是1默认成功
     * @param ids
     */
    void delete(List<Integer> ids);
}
