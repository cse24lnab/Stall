package org.lab.stall_manage.service.impl;

import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.StallService;
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
    public Optional<Stall> findById(Integer id) {
        if(id == null)
        {
            throw new IllegalArgumentException("参数不能为null");
        }
        Stall stall=new Stall();
        stall.setId(id);
        List<Stall> stalls=stallMapper.find(stall);
        if(stalls==null || stalls.isEmpty())
        {
            return Optional.empty();
        }
        //get(0)的值仍可能是null
        return Optional.ofNullable(stalls.get(0));
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

    @Override
    public void update(Stall stall)
    {
        detectUpdate(stall);
        stallMapper.update(stall);
    }

    private void detectUpdate(Stall stall)
    {
        if(stall == null)
        {
            throw new IllegalArgumentException("摊位不能为null");
        }
        if(stall.getId() == null)
        {
            throw new IllegalArgumentException("id不能为null");
        }
        Stall findStall=this.findById(stall.getId()).orElse(null);
        //同add，摊位可能不存在
        if(findStall == null)
        {
            throw new StallNotExistException("摊位不存在");
        }
    }
}
