package org.example.stall_manage.service.impl;

import org.example.stall_manage.mapper.DishMapper;
import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Dish> findAll(Integer stallId) {
        if(stallId==null){
            return Collections.emptyList();
        }
        List<Dish> dishes = dishMapper.findAll(stallId);
        //防御性编程，根据api规范不应该给前端返回null列表
        if(dishes==null){
            return Collections.emptyList();
        }
        return dishes;
    }

    @Override
    public Optional<Dish> find(Integer stallId, String name)
    {
        //防御性编程
        if(stallId==null || name==null)
        {
            return  Optional.empty();
        }
        Dish dish= dishMapper.find(stallId,name);
        //给dish包装，防止返回null
        return Optional.ofNullable(dish);
    }

    @Override
    public void add(Dish dish)
    {
        //默认售罄
        if(dish.getIsSoldOut()==null)
        {
            dish.setIsSoldOut(1);
        }
        try
        {
            dishMapper.add(dish);
        }
        catch (DuplicateKeyException e)
        {
           throw new RuntimeException("该菜品已存在");
        }
    }
}
