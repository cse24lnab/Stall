package org.lab.stall_manage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.exception.DishNotExistException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.impl.DishServiceImpl;
import org.lab.stall_manage.vo.PageVO;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {
    @Mock
    private DishMapper dishMapper;

    @Mock
    private StallMapper stallMapper;

    @InjectMocks
    private DishServiceImpl dishService;

    @Test
    void findReturnsEmptyPageWhenMapperReturnsNull() {
        Dish query = new Dish();
        when(dishMapper.find(query)).thenReturn(null);

        PageVO<Dish> result = dishService.find(1, 10, query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(dishMapper).find(query);
    }

    @Test
    void findReturnsEmptyPageWhenMapperReturnsEmptyList() {
        Dish query = new Dish();
        when(dishMapper.find(query)).thenReturn(Collections.emptyList());

        PageVO<Dish> result = dishService.find(1, 10, query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(dishMapper).find(query);
    }

    @Test
    void findReturnsPageWhenMapperReturnsRows() {
        Dish query = new Dish();
        when(dishMapper.find(query)).thenReturn(List.of(
                createDish("dish-a", "12.50"),
                createDish("dish-b", "15.00")));

        PageVO<Dish> result = dishService.find(1, 10, query);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("dish-a", result.getRecords().get(0).getName());
        verify(dishMapper).find(query);
    }

    @Test
    void addThrowsWhenStallDoesNotExist() {
        Dish dish = createDish("dish-a", "12.50");
        when(stallMapper.find(any(Stall.class))).thenReturn(Collections.emptyList());

        assertThrows(StallNotExistException.class, () -> dishService.add(dish));
        verify(dishMapper, never()).add(any(Dish.class));
    }

    @Test
    void addDefaultsSoldOutToZeroWhenMissing() {
        Dish dish = createDish("dish-a", "12.50");
        dish.setIsSoldOut(null);
        when(stallMapper.find(any(Stall.class))).thenReturn(List.of(new Stall()));

        dishService.add(dish);

        assertEquals(0, dish.getIsSoldOut());
        verify(dishMapper).add(dish);
    }

    @Test
    void addCallsMapperOnceWhenDishIsValid() {
        Dish dish = createDish("dish-a", "12.50");
        dish.setIsSoldOut(0);
        when(stallMapper.find(any(Stall.class))).thenReturn(List.of(new Stall()));

        dishService.add(dish);

        assertEquals(0, dish.getIsSoldOut());
        verify(dishMapper).add(dish);
    }

    @Test
    void deleteByIdDoesNothingWhenIdsIsNull() {
        dishService.deleteById(null);

        verify(dishMapper, never()).deleteById(anyList());
    }

    @Test
    void deleteByIdDoesNothingWhenIdsIsEmpty() {
        dishService.deleteById(Collections.emptyList());

        verify(dishMapper, never()).deleteById(anyList());
    }

    @Test
    void deleteByIdCallsMapperWhenIdsPresent() {
        List<Integer> ids = List.of(1, 2);

        dishService.deleteById(ids);

        verify(dishMapper).deleteById(ids);
    }

    @Test
    void updateThrowsWhenDishIsNull() {
        assertThrows(IllegalArgumentException.class, () -> dishService.update(null));
        verify(dishMapper, never()).update(any(Dish.class));
    }

    @Test
    void updateThrowsWhenIdIsNull() {
        Dish dish = new Dish();
        dish.setName("dish-a");

        assertThrows(IllegalArgumentException.class, () -> dishService.update(dish));
        verify(dishMapper, never()).update(any(Dish.class));
    }

    @Test
    void updateThrowsWhenStallIdProvided() {
        Dish dish = new Dish();
        dish.setId(1);
        dish.setStallId(2);

        assertThrows(IllegalArgumentException.class, () -> dishService.update(dish));
        verify(dishMapper, never()).update(any(Dish.class));
    }

    @Test
    void updateThrowsWhenDishDoesNotExist() {
        Dish dish = new Dish();
        dish.setId(1);
        when(dishMapper.find(any(Dish.class))).thenReturn(Collections.emptyList());

        assertThrows(DishNotExistException.class, () -> dishService.update(dish));
        verify(dishMapper, never()).update(any(Dish.class));
    }

    @Test
    void updateCallsMapperWhenDishIsValid() {
        Dish dish = new Dish();
        dish.setId(1);
        dish.setName("dish-renamed");
        when(dishMapper.find(any(Dish.class))).thenReturn(List.of(createDish("dish-a", "12.50")));

        dishService.update(dish);

        verify(dishMapper).update(dish);
    }

    private Dish createDish(String name, String price) {
        Dish dish = new Dish();
        dish.setStallId(1);
        dish.setName(name);
        dish.setPrice(new BigDecimal(price));
        return dish;
    }
}
