package org.example.stall_manage;

import org.example.stall_manage.exception.StallNotExistException;
import org.example.stall_manage.mapper.DishMapper;
import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Dish;
import org.example.stall_manage.pojo.Stall;
import org.example.stall_manage.service.impl.DishServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DishServiceTest {
    @Mock
    private DishMapper dishMapper;

    @Mock
    private StallMapper stallMapper;

    @InjectMocks
    private DishServiceImpl dishService;

    @Test
    void findReturnsEmptyListWhenStallIdIsMissing() {
        Dish query = new Dish();

        List<Dish> result = dishService.find(query);

        assertTrue(result.isEmpty());
        verifyNoInteractions(dishMapper);
    }

    @Test
    void findReturnsEmptyListWhenMapperReturnsNull() {
        Dish query = new Dish();
        query.setStallId(1);
        when(dishMapper.find(query)).thenReturn(null);

        List<Dish> result = dishService.find(query);

        assertTrue(result.isEmpty());
        verify(dishMapper).find(query);
    }

    @Test
    void addThrowsWhenStallDoesNotExist() {
        Dish dish = createDish();
        when(stallMapper.find(any(Stall.class))).thenReturn(Collections.emptyList());

        assertThrows(StallNotExistException.class, () -> dishService.add(dish));
        verify(dishMapper, never()).add(any(Dish.class));
    }

    @Test
    void addDefaultsSoldOutToOneWhenMissing() {
        Dish dish = createDish();
        dish.setIsSoldOut(null);
        when(stallMapper.find(any(Stall.class))).thenReturn(List.of(new Stall()));

        dishService.add(dish);

        assertEquals(1, dish.getIsSoldOut());
        verify(dishMapper).add(dish);
    }

    @Test
    void addCallsMapperOnceWhenDishIsValid() {
        Dish dish = createDish();
        dish.setIsSoldOut(0);
        when(stallMapper.find(any(Stall.class))).thenReturn(List.of(new Stall()));

        dishService.add(dish);

        assertEquals(0, dish.getIsSoldOut());
        verify(dishMapper).add(dish);
    }

    private Dish createDish() {
        Dish dish = new Dish();
        dish.setStallId(1);
        dish.setName("招牌烤冷面");
        dish.setPrice(new BigDecimal("12.50"));
        return dish;
    }
}
