package org.lab.stall_manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.exception.DishNotExistException;
import org.lab.stall_manage.exception.ForbiddenException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.pojo.enums.UserRole;
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
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private DishServiceImpl dishService;

    @AfterEach
    void clearContext() {
        BaseContext.RemoveCurrentUser();
    }

    @Test
    void adminFindsAllDishes() {
        login(3, UserRole.ADMIN);
        Dish query = new Dish();
        when(dishMapper.findForManagement(query, null)).thenReturn(List.of(dish(1, 1)));

        PageVO<Dish> result = dishService.find(1, 10, query);

        assertEquals(1, result.getTotal());
        verify(dishMapper).findForManagement(query, null);
    }

    @Test
    void merchantFindsOnlyOwnedDishes() {
        login(2, UserRole.MERCHANT);
        Dish query = new Dish();
        when(dishMapper.findForManagement(query, 2)).thenReturn(Collections.emptyList());

        PageVO<Dish> result = dishService.find(1, 10, query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(dishMapper).findForManagement(query, 2);
    }

    @Test
    void userCannotQueryDishes() {
        login(1, UserRole.USER);

        assertThrows(ForbiddenException.class,
                () -> dishService.find(1, 10, new Dish()));
        verify(dishMapper, never()).findForManagement(any(), any());
    }

    @Test
    void merchantCanReadOwnedDish() {
        login(2, UserRole.MERCHANT);
        when(dishMapper.findById(1)).thenReturn(dish(1, 1));
        when(stallMapper.findById(1)).thenReturn(stall(1, 2));

        assertTrue(dishService.findById(1).isPresent());
    }

    @Test
    void merchantCannotReadOtherDish() {
        login(2, UserRole.MERCHANT);
        when(dishMapper.findById(3)).thenReturn(dish(3, 2));
        when(stallMapper.findById(2)).thenReturn(stall(2, 4));

        assertThrows(ForbiddenException.class, () -> dishService.findById(3));
    }

    @Test
    void adminAddsDishForValidMerchantStall() {
        login(3, UserRole.ADMIN);
        Dish dish = newDish(1);
        dish.setIsSoldOut(null);
        when(stallMapper.findById(1)).thenReturn(stall(1, 2));
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));

        dishService.add(dish);

        assertEquals(0, dish.getIsSoldOut());
        verify(dishMapper).add(dish);
    }

    @Test
    void adminCannotAddDishWhenOwnerDoesNotExist() {
        login(3, UserRole.ADMIN);
        Dish dish = newDish(1);
        when(stallMapper.findById(1)).thenReturn(stall(1, 99));
        when(userMapper.find(99)).thenReturn(null);

        assertThrows(UserNotExistException.class, () -> dishService.add(dish));
        verify(dishMapper, never()).add(any());
    }

    @Test
    void adminCannotAddDishWhenOwnerIsNotMerchant() {
        login(3, UserRole.ADMIN);
        Dish dish = newDish(1);
        when(stallMapper.findById(1)).thenReturn(stall(1, 1));
        when(userMapper.find(1)).thenReturn(user(1, UserRole.USER));

        assertThrows(IllegalArgumentException.class, () -> dishService.add(dish));
        verify(dishMapper, never()).add(any());
    }

    @Test
    void merchantAddsDishToOwnedStall() {
        login(2, UserRole.MERCHANT);
        Dish dish = newDish(1);
        when(stallMapper.findById(1)).thenReturn(stall(1, 2));
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));

        dishService.add(dish);

        verify(dishMapper).add(dish);
    }

    @Test
    void merchantCannotAddDishToOtherStall() {
        login(2, UserRole.MERCHANT);
        Dish dish = newDish(2);
        when(stallMapper.findById(2)).thenReturn(stall(2, 4));
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));

        assertThrows(ForbiddenException.class, () -> dishService.add(dish));
        verify(dishMapper, never()).add(any());
    }

    @Test
    void addRejectsMissingStall() {
        login(3, UserRole.ADMIN);
        Dish dish = newDish(99);
        when(stallMapper.findById(99)).thenReturn(null);

        assertThrows(StallNotExistException.class, () -> dishService.add(dish));
    }

    @Test
    void adminDeletesAnyExistingDishes() {
        login(3, UserRole.ADMIN);
        when(dishMapper.findManageableId(List.of(1, 3), null)).thenReturn(List.of(1, 3));

        dishService.deleteById(List.of(1, 3));

        verify(dishMapper).deleteById(List.of(1, 3));
    }

    @Test
    void merchantDeletesOwnedDishesAndDeduplicatesIds() {
        login(2, UserRole.MERCHANT);
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));
        when(dishMapper.findManageableId(List.of(1, 2), 2)).thenReturn(List.of(1, 2));

        dishService.deleteById(List.of(1, 1, 2));

        verify(dishMapper).deleteById(List.of(1, 2));
    }

    @Test
    void merchantCannotDeleteOtherDish() {
        login(2, UserRole.MERCHANT);
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));
        when(dishMapper.findManageableId(List.of(3), 2)).thenReturn(Collections.emptyList());

        assertThrows(ForbiddenException.class, () -> dishService.deleteById(List.of(3)));
        verify(dishMapper, never()).deleteById(anyList());
    }

    @Test
    void mixedOwnerDeleteRejectsWholeBatch() {
        login(2, UserRole.MERCHANT);
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));
        when(dishMapper.findManageableId(List.of(1, 3), 2)).thenReturn(List.of(1));

        assertThrows(ForbiddenException.class, () -> dishService.deleteById(List.of(1, 3)));
        verify(dishMapper, never()).deleteById(anyList());
    }

    @Test
    void nullDeleteDoesNothing() {
        dishService.deleteById(null);
        verify(dishMapper, never()).deleteById(anyList());
    }

    @Test
    void adminUpdatesAnyDish() {
        login(3, UserRole.ADMIN);
        Dish update = updateDish(1);
        when(dishMapper.findById(1)).thenReturn(dish(1, 1));

        dishService.update(update);

        verify(dishMapper).update(update);
        verify(stallMapper, never()).findById(any());
    }

    @Test
    void merchantUpdatesOwnedDish() {
        login(2, UserRole.MERCHANT);
        Dish update = updateDish(1);
        when(dishMapper.findById(1)).thenReturn(dish(1, 1));
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));
        when(stallMapper.findById(1)).thenReturn(stall(1, 2));

        dishService.update(update);

        verify(dishMapper).update(update);
    }

    @Test
    void merchantCannotUpdateOtherDish() {
        login(2, UserRole.MERCHANT);
        Dish update = updateDish(3);
        when(dishMapper.findById(3)).thenReturn(dish(3, 2));
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));
        when(stallMapper.findById(2)).thenReturn(stall(2, 4));

        assertThrows(ForbiddenException.class, () -> dishService.update(update));
        verify(dishMapper, never()).update(any());
    }

    @Test
    void userCannotUpdateDish() {
        login(1, UserRole.USER);
        Dish update = updateDish(1);
        when(dishMapper.findById(1)).thenReturn(dish(1, 1));

        assertThrows(ForbiddenException.class, () -> dishService.update(update));
        verify(dishMapper, never()).update(any());
    }

    @Test
    void updateRejectsNullDish() {
        assertThrows(IllegalArgumentException.class, () -> dishService.update(null));
    }

    @Test
    void updateRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> dishService.update(new Dish()));
    }

    @Test
    void updateRejectsStallId() {
        Dish update = updateDish(1);
        update.setStallId(2);

        assertThrows(IllegalArgumentException.class, () -> dishService.update(update));
    }

    @Test
    void updateRejectsMissingDish() {
        Dish update = updateDish(99);
        when(dishMapper.findById(99)).thenReturn(null);

        assertThrows(DishNotExistException.class, () -> dishService.update(update));
    }

    @Test
    void updateRejectsMissingStall() {
        login(2, UserRole.MERCHANT);
        Dish update = updateDish(1);
        when(dishMapper.findById(1)).thenReturn(dish(1, 99));
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));
        when(stallMapper.findById(99)).thenReturn(null);

        assertThrows(StallNotExistException.class, () -> dishService.update(update));
    }

    private void login(int id, UserRole role) {
        BaseContext.setCurrentUser(new CurrentUser(id, "test", role));
    }

    private Dish dish(int id, int stallId) {
        Dish dish = newDish(stallId);
        dish.setId(id);
        return dish;
    }

    private Dish newDish(int stallId) {
        Dish dish = new Dish();
        dish.setStallId(stallId);
        dish.setName("dish");
        dish.setPrice(new BigDecimal("12.50"));
        return dish;
    }

    private Dish updateDish(int id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName("renamed");
        return dish;
    }

    private Stall stall(int id, int ownerUserId) {
        Stall stall = new Stall();
        stall.setId(id);
        stall.setOwnerUserId(ownerUserId);
        return stall;
    }

    private User user(int id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
