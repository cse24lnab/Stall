package org.example.stall_manage;

import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.StallService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class StallServiceTest {
    @Autowired
    private StallService stallService;

    @Test
    public void findAll() {
        System.out.println("开始测试--------------");
        List<Stall> stalls = stallService.findAll();
        Assertions.assertNotNull(stalls);
        Assertions.assertEquals(1,stalls.size());
        Assertions.assertEquals("烤冷面",stalls.get(0).getName());
        System.out.println("结束测试--------------");
    }

}
