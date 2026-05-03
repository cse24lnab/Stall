package org.example.stall_manage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.stall_manage.pojo.Result;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class StallController {

    @Autowired
    private StallService stallService;


    @GetMapping("/stalls")
    public Result<List<Stall>> find( Stall stall)
    {
        log.info("查询小摊，条件为{}",stall);
        List<Stall> result=stallService.find(stall);
        return Result.success(result);
    }

    @PostMapping("/stalls")
    public Result<Stall> add(@RequestBody @Valid Stall stall)
    {
        log.info("增加小摊，名字叫{}",stall.getName());
        stallService.add(stall);
        return Result.success();
    }

    @DeleteMapping("/stalls")
    public Result<Stall> delete(@RequestParam List<Integer> ids)
    {
        log.info("删除小摊，名字列表是{}",ids);
        stallService.delete(ids);
        return Result.success();
    }
}
