package org.lab.stall_manage.service;

import org.lab.stall_manage.pojo.Stall;

import java.util.List;
import java.util.Optional;

public interface StallService {
    /**
     * 根据输入查找摊位，目前动态查询了id和name
     * @param stall
     * @return List<Stall>
     */
    List<Stall> find(Stall stall);

    /**
     * 根据指定id查找摊位,查询回显
     * @param id
     * @return Option<Stall>
     */
    Optional<Stall> findById(Integer id);

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

    /**
     * 更新摊位信息
     * @param
     * @return
     */
    void update(Stall stall);
}
