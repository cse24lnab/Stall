package org.lab.stall_manage.mapper;

import org.apache.ibatis.annotations.Param;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.service.StallService;

import java.util.List;

public interface DishMapper {
    //todo 分页查询和根据价格降序
    //todo 名字模糊查询
    List<Dish> find(Dish dish);

    Dish findById(@Param("id") Integer id);

    List<Dish> findForManagement(@Param("dish") Dish dish,
                                 @Param("ownerUserId") Integer ownerUserId);

    List<Integer> findManageableId(@Param("ids") List<Integer> ids,
                                   @Param("ownerUserId") Integer ownerUserId);

    int add(Dish dish);

    int deleteById(List<Integer> ids);

    /**
     * 目前是用来给stall的删除操作使用，同一事务
     * @see StallService
     */
    int deleteByStallId(List<Integer> stallIds);

    int update(Dish dish);
}
