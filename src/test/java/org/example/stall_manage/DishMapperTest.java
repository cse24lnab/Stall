package org.example.stall_manage;

import org.example.stall_manage.mapper.DishMapper;
import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DishMapperTest {
    @Autowired
    private DishMapper dishMapper;

   /* @Test
    void findAllDish() {
        System.out.println("开始测试dish--------------");
        List<Dish> dishs = dishMapper.findAll(1);
        Assertions.assertNotNull(dishs);
        Assertions.assertEquals(1,dishs.size());
        Assertions.assertEquals("基础款烤冷面",dishs.get(0).getName());
    }*/
}
