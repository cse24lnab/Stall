package org.lab.stall_manage.service;

import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.vo.PageVO;

import java.util.List;
import java.util.Optional;

public interface DishService {
    /**
     * 查询菜品
     */
    PageVO<Dish> find(int page, int pageSize, Dish dish);
    /**
     * 根据id查询特定菜品
     * @throws IllegalArgumentException id不能为空
     */
    Optional<Dish> findById(Integer id);

    //todo is_delete这个逻辑删除位加上名字是unique_key,那删除了的菜的名字还是用不了，算是个bug
    /**
     * 增加菜品
     * @throws org.lab.stall_manage.exception.StallNotExistException 摊位不存在
     */
    void add (Dish dish);

    /**
     * 删除逻辑和stall的一样
     * @see StallService
     */
    void deleteById(List<Integer> ids);

    /**
     * 修改菜品，其中stallId不可修改
     * @throws org.lab.stall_manage.exception.DishNotExistException 菜品不存在
     * @throws IllegalArgumentException 菜品不能为空 id不能为空 stallId不可修改
     */
    void update(Dish dish);
}
