package org.lab.stall_manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.exception.ForbiddenException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.impl.StallServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StallServiceTest {
    @Mock
    private StallMapper stallMapper;
    @Mock
    private DishMapper dishMapper;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private StallServiceImpl stallService;

    @AfterEach
    void clearContext() {
        BaseContext.RemoveCurrentUser();
    }

    @Test
    void adminFindsAllStalls() {
        login(3, UserRole.ADMIN);
        Stall query = new Stall();
        when(stallMapper.find(query)).thenReturn(List.of(stall(1, 2)));

        assertEquals(1, stallService.find(1, 10, query).getTotal());
        assertEquals(null, query.getOwnerUserId());
    }

    @Test
    void merchantFindsOnlyOwnedStalls() {
        login(2, UserRole.MERCHANT);
        Stall query = new Stall();
        when(stallMapper.find(query)).thenReturn(Collections.emptyList());

        assertTrue(stallService.find(1, 10, query).getRecords().isEmpty());
        assertEquals(2, query.getOwnerUserId());
    }

    @Test
    void userCannotFindStalls() {
        login(1, UserRole.USER);

        assertThrows(ForbiddenException.class,
                () -> stallService.find(1, 10, new Stall()));
    }

    @Test
    void merchantCanReadOwnedStall() {
        login(2, UserRole.MERCHANT);
        when(stallMapper.findById(1)).thenReturn(stall(1, 2));

        assertTrue(stallService.findById(1).isPresent());
    }

    @Test
    void merchantCannotReadOtherStall() {
        login(2, UserRole.MERCHANT);
        when(stallMapper.findById(2)).thenReturn(stall(2, 4));

        assertThrows(ForbiddenException.class, () -> stallService.findById(2));
    }

    @Test
    void adminAddsStallForMerchant() {
        login(3, UserRole.ADMIN);
        Stall stall = stall(null, 2);
        when(userMapper.find(2)).thenReturn(user(2, UserRole.MERCHANT));

        stallService.add(stall);

        assertEquals(0, stall.getCurrentStatus());
        verify(stallMapper).add(stall);
    }

    @Test
    void adminCannotAssignStallToNonMerchant() {
        login(3, UserRole.ADMIN);
        Stall stall = stall(null, 1);
        when(userMapper.find(1)).thenReturn(user(1, UserRole.USER));

        assertThrows(IllegalArgumentException.class, () -> stallService.add(stall));
        verify(stallMapper, never()).add(any());
    }

    @Test
    void merchantCannotAddStall() {
        login(2, UserRole.MERCHANT);

        assertThrows(ForbiddenException.class,
                () -> stallService.add(stall(null, 2)));
    }

    @Test
    void adminDeletesStallsAndDishes() {
        login(3, UserRole.ADMIN);

        stallService.delete(List.of(1, 2));

        verify(stallMapper).delete(List.of(1, 2));
        verify(dishMapper).deleteByStallId(List.of(1, 2));
    }

    @Test
    void merchantCannotDeleteStall() {
        login(2, UserRole.MERCHANT);

        assertThrows(ForbiddenException.class, () -> stallService.delete(List.of(1)));
    }

    @Test
    void adminUpdatesExistingStall() {
        login(3, UserRole.ADMIN);
        Stall update = stall(1, null);
        when(stallMapper.findById(1)).thenReturn(stall(1, 2));

        stallService.update(update);

        verify(stallMapper).update(update);
    }

    @Test
    void updateRejectsOwnerChange() {
        login(3, UserRole.ADMIN);
        Stall update = stall(1, 4);

        assertThrows(IllegalArgumentException.class, () -> stallService.update(update));
    }

    @Test
    void updateRejectsMissingStall() {
        login(3, UserRole.ADMIN);
        Stall update = stall(99, null);
        when(stallMapper.findById(99)).thenReturn(null);

        assertThrows(StallNotExistException.class, () -> stallService.update(update));
    }

    private void login(int id, UserRole role) {
        BaseContext.setCurrentUser(new CurrentUser(id, "test", role));
    }

    private Stall stall(Integer id, Integer ownerUserId) {
        Stall stall = new Stall();
        stall.setId(id);
        stall.setName("stall");
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
