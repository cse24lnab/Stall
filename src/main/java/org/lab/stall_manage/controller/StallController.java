package org.lab.stall_manage.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.annotation.RequireRole;
import org.lab.stall_manage.pojo.Result;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.StallService;
import org.lab.stall_manage.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@Validated
@RestController
public class StallController {

    @Autowired
    private StallService stallService;

    @GetMapping("/stalls")
    public Result<PageVO<Stall>> find(
            Stall stall,
            @RequestParam(defaultValue = "1") @Positive(message = "page must be greater than 0") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "pageSize must be greater than 0") int pageSize)
    {
        log.info("查询小摊，条件为{}",stall);
        PageVO<Stall> result=stallService.find(page,pageSize,stall);
        return Result.success(result);
    }

    @GetMapping("/stalls/{id}")
    public Result<Stall> findById(@PathVariable @Positive(message = "id必须大于0") Integer id)
    {
        log.info("查询id为{}的小摊",id);
        Stall stall=stallService.findById(id).orElse(null);
        return Result.success(stall);
    }

    @RequireRole({UserRole.ADMIN})
    @PostMapping("/stalls")
    public Result<?> add(@RequestBody @Valid Stall stall)
    {
        log.info("增加小摊，名字叫{}",stall.getName());
        stallService.add(stall);
        return Result.success();
    }

    @RequireRole({UserRole.ADMIN})
    @DeleteMapping("/stalls")
    public Result<?> delete(@RequestParam List<Integer> ids)
    {
        log.info("删除小摊，名字列表是{}",ids);
        stallService.delete(ids);
        return Result.success();
    }

    @RequireRole({UserRole.ADMIN})
    @PutMapping("/stalls/{id}")
    public  Result<?> update(@PathVariable @Positive(message = "id必须大于0") Integer id,@RequestBody Stall stall)
    {
        if(!Objects.equals(id, stall.getId()) && stall.getId()!= null)
        {
            //todo 是否要自定义异常
            throw new RuntimeException("id不一致");
        }
        stall.setId(id);
        stallService.update(stall);
        return Result.success();
    }
}
