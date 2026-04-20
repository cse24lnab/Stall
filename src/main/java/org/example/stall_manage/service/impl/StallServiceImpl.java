package org.example.stall_manage.service.impl;

import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class StallServiceImpl implements StallService {
    @Autowired
    private StallMapper stallMapper;

    @Override
    public List<Stall> findAll() {
        //如果stall表没有stall可能返回null
        List<Stall>stalls = stallMapper.findAll();
        //防御型编程不返回null
        if(stalls==null){
            return Collections.emptyList();
        }
        else{
            return stalls;
        }
    }

    @Override
    public Optional<Stall> find(String name){
        //名字不能为空，防御性编程
        if(name==null)
        {
           return Optional.empty();
        }
        Stall stall= stallMapper.find(name);
        //如果为空返回Optional.empty();如果不为空返回包装过的stall
        return Optional.ofNullable(stall);
    }

    @Override
    public void add(Stall stall)
    {
        //status默认值
        if(stall.getCurrentStatus()==null)
        {
             stall.setCurrentStatus(0);
        }
        //捕获异常，可能创建失败
        try
        {
            stallMapper.add(stall);
        }
        catch (DuplicateKeyException e)
        {
            throw  new RuntimeException("摊位名字不能重复");
        }
    }
}
