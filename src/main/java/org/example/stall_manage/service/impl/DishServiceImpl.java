package org.example.stall_manage.service.impl;

import org.example.stall_manage.exception.StallNotExistException;
import org.example.stall_manage.mapper.DishMapper;
import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private StallMapper stallMapper;

    @Override
    public List<Dish> find(Dish dish) {
        if(dish.getStallId()==null){
            return Collections.emptyList();
        }
        List<Dish> dishes = dishMapper.find(dish);
        //防御性编程，根据api规范不应该给前端返回null列表
        if(dishes==null){
            return Collections.emptyList();
        }
        return dishes;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(Dish dish) {
        //摊位首先要存在
        Stall stall=new Stall();
        stall.setId(dish.getStallId());
        List<Stall> findStall=stallMapper.find(stall);
        if(findStall==null || findStall.isEmpty())
        {
            //由于全局异常处理器的捕获级别，这里必须自定义异常并抛出
            throw new StallNotExistException("摊位不存在");
        }
        //isSoldOut的默认值给sql管理
        dishMapper.add(dish);
    }

    @Override
    public void deleteById(List<Integer> ids) {
        dishMapper.deleteById(ids);
    }
}
