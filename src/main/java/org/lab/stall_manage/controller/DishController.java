package org.lab.stall_manage.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.annotation.RequireRole;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.pojo.Result;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
//json参数，单一参数都可用，支持分组
@Validated
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/dishes")
    public Result<List<Dish>> find(Dish dish)
    {
        log.info("查询菜品，条件为{}",dish);
        List<Dish>result = dishService.find(dish);
        return Result.success(result);
    }

    @GetMapping("/dishes/{id}")
    public Result<Dish> findById(@PathVariable @Positive(message = "id必须大于0") Integer id)
    {
        log.info("查询菜品，id为{}",id);
        Dish dish= dishService.findById(id).orElse(null);
        return Result.success(dish);
    }

    @RequireRole({UserRole.ADMIN,UserRole.MERCHANT})
    @PostMapping("/dishes")
    //@Valid只可以用于json参数
    public Result<Dish> add(@RequestBody @Valid Dish dish)
    {
        log.info("增加名字为{}的菜品",dish.getName());
        //检查摊位是否存在
        dishService.add(dish);
        return Result.success();
    }

    @RequireRole({UserRole.ADMIN,UserRole.MERCHANT})
    @DeleteMapping("/dishes")
    public Result<Dish> delete(@RequestParam List<Integer> ids)
    {
        log.info("删除id为{}的菜品",ids);
        dishService.deleteById(ids);
        return Result.success();
    }

    @RequireRole({UserRole.ADMIN,UserRole.MERCHANT})
    @PutMapping("/dishes/{id}")
    public Result<Dish> update(@PathVariable @Positive(message = "id必须大于0") Integer id,@RequestBody Dish dish)
    {
        log.info("修改id为{}的菜品，信息为{}",id,dish);
        if(!Objects.equals(id, dish.getId()) && dish.getId() != null)
        {
            throw new RuntimeException("id不一致");
        }
        dish.setId(id);
        dishService.update(dish);
        return Result.success();
    }
}
