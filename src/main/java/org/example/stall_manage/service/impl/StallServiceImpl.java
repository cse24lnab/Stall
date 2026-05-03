package org.example.stall_manage.service.impl;

import org.example.stall_manage.exception.StallNotExistException;
import org.example.stall_manage.mapper.DishMapper;
import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StallServiceImpl implements StallService {
    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Stall> find(Stall stall) {
        //如果stall表没有stall可能返回null
        List<Stall>stalls = stallMapper.find(stall);
        //防御型编程不返回null
        return Objects.requireNonNullElse(stalls, Collections.emptyList());
    }

    @Override
    public Stall getById(Integer id) {
        if(id == null)
        {
            throw new IllegalArgumentException("参数不能为null");
        }
        Stall stall=new Stall();
        stall.setId(id);
        List<Stall> stalls=stallMapper.find(stall);
        if(stalls==null || stalls.isEmpty())
        {
            throw new StallNotExistException("摊位不存在");
        }
        return stalls.get(0);
    }

    @Override
    public void add(Stall stall)
    {
        if(stall.getCurrentStatus() == null)
        {
            stall.setCurrentStatus(0);
        }
        stallMapper.add(stall);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Integer> ids)
    {
        if(ids == null || ids.isEmpty())
        {
            return;
        }
        stallMapper.delete(ids);
        dishMapper.deleteByStallId(ids);
    }
}
