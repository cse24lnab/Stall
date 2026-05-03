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
        //处理名字为空的情况给前端
        List<Stall> result=stallService.find(stall);
        //把包装好的stall取出来，方便传递给前端
        return Result.success(result);
    }

    @PostMapping("/stalls")
    public Result<Stall> add(@RequestBody @Valid Stall stall)
    {
            log.info("增加小摊，名字叫{}",stall.getName());
            stallService.add(stall);
            return Result.success();
    }
}
