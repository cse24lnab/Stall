package org.lab.stall_manage;

import org.junit.jupiter.api.Test;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.service.DishService;
import org.lab.stall_manage.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = StallManageApplication.class)
@Transactional
class DishServicePaginationIntegrationTest {

    @Autowired
    private DishService dishService;

    @Test
    void findReturnsFirstPage() {
        PageVO<Dish> page = dishService.find(1, 2, new Dish());

        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
        assertEquals(1, page.getRecords().get(0).getId());
        assertEquals(2, page.getRecords().get(1).getId());
    }

    @Test
    void findReturnsSecondPage() {
        PageVO<Dish> page = dishService.find(2, 2, new Dish());

        assertEquals(3, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(3, page.getRecords().get(0).getId());
    }

    @Test
    void findReturnsFilteredPage() {
        Dish query = new Dish();
        query.setStallId(1);

        PageVO<Dish> page = dishService.find(1, 1, query);

        assertEquals(2, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(1, page.getRecords().get(0).getId());
    }
}
