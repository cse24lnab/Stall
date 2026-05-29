package org.lab.stall_manage.service;

import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.vo.PageVO;

import java.util.List;
import java.util.Optional;

public interface StallService {
    /**
     * 根据输入查找摊位，目前动态查询了id和name
     */
     PageVO<Stall> find(int page, int pageSize , Stall stall);

    /**
     * 根据指定id查找摊位,查询回显
     * @throws IllegalArgumentException id不能为空
     */
    Optional<Stall> findById(Integer id);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的摊位的名字还是用不了，算是个bug
    /**
     * 增加摊位
     */
    void add(Stall stall);

    /**
     * 如果is_delete已经是1默认成功
     */
    void delete(List<Integer> ids);

    /**
     * 更新摊位信息
     * @throws org.lab.stall_manage.exception.StallNotExistException 摊位不存在
     * @throws IllegalArgumentException id不能为空 摊位不能为空
     */
    void update(Stall stall);
}
