package org.example.stall_manage;

import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.DishService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DishServiceTest {
    @Autowired
    private DishService dishService;

   /* @Test
    public void findAll() {
        System.out.println("开始测试------------");
        List<Dish> dishList = dishService.findAll(1);
        Assertions.assertNotNull(dishList);
        Assertions.assertEquals(1,dishList.size());
        Assertions.assertEquals("基础款烤冷面",dishList.get(0).getName());
        System.out.println("结束测试------------");
    }*/
}
