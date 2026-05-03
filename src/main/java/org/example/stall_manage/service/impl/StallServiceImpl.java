package org.example.stall_manage.service.impl;

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
    public void add(Stall stall)
    {
        //currentStatus的默认值给sql管理
        stallMapper.add(stall);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Integer> ids)
    {
        stallMapper.delete(ids);
        dishMapper.deleteByStallId(ids);
    }
}
