package org.example.stall_manage.controller;

import jakarta.validation.Valid;
import org.example.stall_manage.pojo.Result;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.StallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class StallController {

    private static final Logger log= LoggerFactory.getLogger(StallController.class);

    @Autowired
    private StallService stallService;

    @GetMapping("/stalls")
    public Result<List<Stall>> findAll()
    {
        log.info("查询所有小摊");
        List<Stall> result = stallService.findAll();
        return Result.success(result);
    }

    @GetMapping("/stall")
    public Result<Stall> find(String name)
    {
        log.info("查询名为{}的小摊",name);
        //处理名字为空的情况给前端
        if(name==null)
        {
            return Result.error("名字不能为空");
        }
        Optional<Stall> Ostall=stallService.find(name);
        //把包装好的stall取出来，方便传递给前端
        Stall stall=Ostall.orElse(null);
        return Result.success(stall);
    }

    @PostMapping("/stalls")
    public Result<Stall> add(@RequestBody @Valid Stall stall)
    {
            log.info("增加小摊，名字叫{}",stall.getName());
            stallService.add(stall);
            return Result.success();
    }
}
