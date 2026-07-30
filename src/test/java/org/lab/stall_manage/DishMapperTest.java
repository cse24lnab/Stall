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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class DishMapperTest {
    @Autowired
    private DishMapper dishMapper;

    @Test
    void findByIdReturnsDish() {
        Dish dish = dishMapper.findById(1);

        assertNotNull(dish);
        assertEquals(1, dish.getStallId());
    }

    @Test
    void adminManagementQueryReturnsAllDishes() {
        List<Dish> dishes = dishMapper.findForManagement(new Dish(), null);

        assertEquals(3, dishes.size());
    }

    @Test
    void merchantManagementQueryReturnsOwnedDishes() {
        List<Dish> dishes = dishMapper.findForManagement(new Dish(), 2);

        assertEquals(2, dishes.size());
        assertTrue(dishes.stream().allMatch(dish -> dish.getStallId() == 1));
    }

    @Test
    void findManageableIdFiltersByOwner() {
        assertEquals(List.of(1, 2), dishMapper.findManageableId(List.of(1, 2, 3), 2));
        assertEquals(List.of(1, 2, 3), dishMapper.findManageableId(List.of(1, 2, 3), null));
    }

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

        int inserted = dishMapper.add(dish);

        assertEquals(1, inserted);
        assertNotNull(dish.getId());

        Dish query = new Dish();
        query.setStallId(2);
        query.setName("测试新品");
        query.setPrice(new BigDecimal("18.80"));
        List<Dish> insertedDishes = dishMapper.find(query);

        assertEquals(1, insertedDishes.size());
        assertEquals(dish.getId(), insertedDishes.get(0).getId());
        assertEquals(1, insertedDishes.get(0).getIsSoldOut());
    }

    @Test
    void deleteByOneExistDishId()
    {
        int deletedDish = dishMapper.deleteById(List.of(1));

        Dish deletedQuery = new Dish();
        deletedQuery.setId(1);
        Dish remainQuery = new Dish();
        remainQuery.setId(2);

        assertEquals(1,deletedDish);
        assertEquals(0, dishMapper.find(deletedQuery).size());
        assertEquals(1, dishMapper.find(remainQuery).size());
        assertEquals("豪华烤冷面", dishMapper.find(remainQuery).get(0).getName());
    }

    @Test
    void deleteByOneExistDishStallId()
    {
        int deletedDish = dishMapper.deleteByStallId(List.of(1));

        Dish remainQuery = new Dish();
        remainQuery.setId(3);

        assertEquals(2,deletedDish);
        assertEquals(1, dishMapper.find(remainQuery).size());
        assertEquals("鸡蛋煎饼", dishMapper.find(remainQuery).get(0).getName());
    }

    @Test
    void deleteByMultiExistDishId()
    {
        int deletedDish = dishMapper.deleteById(List.of(1,2));

        Dish remainQuery = new Dish();
        remainQuery.setId(3);

        assertEquals(2,deletedDish);
        assertEquals(1, dishMapper.find(remainQuery).size());
        assertEquals("鸡蛋煎饼", dishMapper.find(remainQuery).get(0).getName());
    }

    @Test
    void deleteByMultiExistDishStallId()
    {
        int deletedDish = dishMapper.deleteByStallId(List.of(1,2));
        assertEquals(3,deletedDish);
        assertEquals(0,dishMapper.find(new Dish()).size());
    }

    @Test
    void deleteByIdReturnsOneWhenPartiallyExists()
    {
        int deletedDish = dishMapper.deleteById(List.of(1, 999));

        Dish deletedQuery = new Dish();
        deletedQuery.setId(1);

        assertEquals(1, deletedDish);
        assertEquals(0, dishMapper.find(deletedQuery).size());
    }

    @Test
    void deleteByIdReturnsZeroWhenNothingExists()
    {
        int deletedDish = dishMapper.deleteById(List.of(999));

        assertEquals(0, deletedDish);
    }

    @Test
    void deleteByStallIdReturnsZeroWhenNothingExists()
    {
        int deletedDish = dishMapper.deleteByStallId(List.of(999));

        assertEquals(0, deletedDish);
    }

    /*
      1.updateSoldOutAndNullName
      2.updateNameAndPrice
      3.updateNotExistDish
     */
    @Test
    void updateSoldOutAndNullName()
    {
        Dish dish=new Dish();
        dish.setIsSoldOut(1);
        dish.setName("");
        dish.setId(1);
        int update = dishMapper.update(dish);
        List<Dish> dishes = dishMapper.find(dish);
        assertEquals(1,update);
        assertEquals("招牌烤冷面",dishes.get(0).getName());
        assertEquals(1,dishes.get(0).getIsSoldOut());
    }

    @Test
    void updateNameAndPrice()
    {
        Dish dish=new Dish();
        dish.setName("测试");
        dish.setPrice(new BigDecimal("100.0"));
        dish.setId(1);
        int update = dishMapper.update(dish);
        List<Dish> dishes = dishMapper.find(dish);
        assertEquals(1,update);
        assertEquals("测试",dishes.get(0).getName());
        assertEquals(0, dishes.get(0).getPrice().compareTo(BigDecimal.valueOf(100.0)));
    }

    @Test
    void updatePriceOnlyKeepsOtherFields()
    {
        Dish dish = new Dish();
        dish.setId(1);
        dish.setPrice(new BigDecimal("66.60"));

        int update = dishMapper.update(dish);

        Dish query = new Dish();
        query.setId(1);
        List<Dish> dishes = dishMapper.find(query);

        assertEquals(1, update);
        assertEquals(1, dishes.size());
        assertEquals("招牌烤冷面", dishes.get(0).getName());
        assertEquals(0, dishes.get(0).getPrice().compareTo(new BigDecimal("66.60")));
        assertEquals(0, dishes.get(0).getIsSoldOut());
    }

    @Test
    void updateNotExistDish()
    {
        dishMapper.deleteById(List.of(1));
        Dish dish=new Dish();
        dish.setName("测试");
        dish.setPrice(new BigDecimal("100.0"));
        dish.setId(1);
        int update = dishMapper.update(dish);
        assertEquals(0,update);
        assertEquals(0, dishMapper.find(dish).size());
    }
}
