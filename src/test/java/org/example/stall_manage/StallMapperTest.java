package org.example.stall_manage;

import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Stall;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class StallMapperTest {

    @Autowired
    private StallMapper stallMapper;

    @Test
    void testfindAllStall() {
        System.out.println("开始测试findALLStall-------------------");
        List<Stall> stalls = stallMapper.findAll();
        Assertions.assertNotNull(stalls);
        Assertions.assertEquals(1,stalls.size());
        Assertions.assertEquals("烤冷面",stalls.get(0).getName());
        System.out.println("测试结束------------------------------");
    }
}
