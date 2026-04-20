package org.example.stall_manage.controller;

import jakarta.validation.Valid;
import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Result;
import org.example.stall_manage.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/dishes")
    public Result<List<Dish>> findAll(Integer stallId)
    {
        if(stallId==null)
        {
            return Result.error("id不能为空");
        }
        List<Dish>result = dishService.findAll(stallId);
        return Result.success(result);
    }

    @GetMapping("/dish")
    public Result<Dish> find(Integer stallId,String name)
    {
        if(stallId==null || name==null)
        {
            return Result.error("id或者名字不能为空");
        }
        Optional<Dish> Odish=dishService.find(stallId,name);
        //把包装取出来以正确传给前端
        Dish dish=Odish.orElse(null);
        return Result.success(dish);
    }

    @PostMapping("/dishes")
    public Result<Dish> add(@RequestBody @Valid Dish dish)
    {
            dishService.add(dish);
            return Result.success();
    }
}
