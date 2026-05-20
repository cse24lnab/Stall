package org.lab.stall_manage.service.impl;

import org.lab.stall_manage.exception.DishNotExistException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private StallMapper stallMapper;

    @Override
    public List<Dish> find(Dish dish) {
        List<Dish> dishes = dishMapper.find(dish);
        //防御性编程，根据api规范不应该给前端返回null列表
        if(dishes==null){
            return Collections.emptyList();
        }
        return dishes;
    }

    @Override
    public Optional<Dish> findById(Integer id) {
        if(id == null)
        {
            throw new IllegalArgumentException("id不能为空");
        }
        Dish dish=new Dish();
        dish.setId(id);
        List<Dish> dishes=dishMapper.find(dish);
        if(dishes == null || dishes.isEmpty())
        {
            return Optional.empty();
        }
        //get(0)的值仍可能是null
        return Optional.ofNullable(dishes.get(0));
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
        if(dish.getIsSoldOut()==null)
        {
            dish.setIsSoldOut(0);
        }
        dishMapper.add(dish);
    }

    @Override
    public void deleteById(List<Integer> ids) {
        if(ids == null || ids.isEmpty())
        {
            return;
        }
        dishMapper.deleteById(ids);
    }

    @Override
    public void update(Dish dish) {
        detectUpdate(dish);
        dishMapper.update(dish);
    }

    private void detectUpdate(Dish dish)
    {
        if (dish == null)
        {
            throw new IllegalArgumentException("菜品不能为空");
        }
        if(dish.getId() == null)
        {
            throw new IllegalArgumentException("id不能为空");
        }
        if(dish.getStallId() !=null)
        {
            throw new IllegalArgumentException("stallId不可修改");
        }
        Dish findDish=this.findById(dish.getId()).orElse(null);
        //同add,dish首先要存在
        if(findDish == null)
        {
            throw  new DishNotExistException("菜品不存在");
        }
    }
}
