package org.example.stall_manage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Result;
import org.example.stall_manage.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/dishes")
    public Result<List<Dish>> find(@Valid Dish dish)
    {
        log.info("查询菜品，条件为{}",dish);
        List<Dish>result = dishService.find(dish);
        return Result.success(result);
    }

    @PostMapping("/dishes")
    public Result<Dish> add(@RequestBody @Valid Dish dish)
    {
            log.info("增加名字为{}的菜品",dish.getName());
            //检查摊位是否存在
            dishService.add(dish);
            return Result.success();
    }

    @DeleteMapping("/dishes")
    public Result<Dish> delete(@RequestParam List<Integer> ids)
    {
        log.info("删除id为{}的菜品",ids);
        dishService.deleteById(ids);
        return Result.success();
    }
}
