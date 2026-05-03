package org.example.stall_manage.service.impl;

import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StallServiceImpl implements StallService {
    @Autowired
    private StallMapper stallMapper;

    @Override
    public List<Stall> find(Stall stall) {
        //如果stall表没有stall可能返回null
        List<Stall>stalls = stallMapper.find(stall);
        //防御型编程不返回null
        if(stalls==null){
            return Collections.emptyList();
        }
        else{
            return stalls;
        }
    }

    @Override
    public void add(Stall stall)
    {
        //status默认值
        if(stall.getCurrentStatus()==null)
        {
             stall.setCurrentStatus(0);
        }
        stallMapper.add(stall);
    }
}
