package org.lab.stall_manage;

import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.pojo.Dish;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class DishMapperTest {
    @Autowired
    private DishMapper dishMapper;

    @Test
    void findByStallId() {
        Dish query = new Dish();
        query.setStallId(1);

        List<Dish> dishes = dishMapper.find(query);

        assertEquals(2, dishes.size());
        for (Dish dish : dishes) {
            assertEquals(1, dish.getStallId());
        }
    }

    @Test
    void findByNameMapsUnderscoreFields() {
        Dish query = new Dish();
        query.setStallId(1);
        query.setName("招牌烤冷面");

        List<Dish> dishes = dishMapper.find(query);

        assertEquals(1, dishes.size());
        Dish dish = dishes.get(0);
        assertEquals(1, dish.getStallId());
        assertEquals(0, dish.getIsSoldOut());
    }

    @Test
    void findByPrice() {
        Dish query = new Dish();
        query.setStallId(1);
        query.setPrice(new BigDecimal("15.00"));

        List<Dish> dishes = dishMapper.find(query);

        assertEquals(1, dishes.size());
        assertEquals("豪华烤冷面", dishes.get(0).getName());
    }

    @Test
    void findByMultipleConditions() {
        Dish query = new Dish();
        query.setStallId(1);
        query.setName("招牌烤冷面");
        query.setPrice(new BigDecimal("12.50"));

        List<Dish> dishes = dishMapper.find(query);

        assertEquals(1, dishes.size());
        assertEquals("招牌烤冷面", dishes.get(0).getName());
    }

    @Test
    void addAssignsGeneratedId() {
        Dish dish = new Dish();
        dish.setStallId(2);
        dish.setName("测试新品");
        dish.setPrice(new BigDecimal("18.80"));
        dish.setIsSoldOut(1);

        dishMapper.add(dish);

        assertNotNull(dish.getId());

        Dish query = new Dish();
        query.setStallId(2);
        query.setName("测试新品");
        query.setPrice(new BigDecimal("18.80"));
        List<Dish> inserted = dishMapper.find(query);

        assertEquals(1, inserted.size());
        assertEquals(dish.getId(), inserted.get(0).getId());
        assertEquals(1, inserted.get(0).getIsSoldOut());
    }
}
