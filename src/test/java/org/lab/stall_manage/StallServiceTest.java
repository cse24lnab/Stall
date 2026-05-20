package org.lab.stall_manage;

import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.impl.StallServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StallServiceTest {
    @Mock
    private StallMapper stallMapper;

    @Mock
    private DishMapper dishMapper;

    @InjectMocks
    private StallServiceImpl stallService;

    @Test
    void findReturnsEmptyListWhenMapperReturnsNull() {
        Stall query = new Stall();
        when(stallMapper.find(query)).thenReturn(null);

        List<Stall> result = stallService.find(query);

        assertTrue(result.isEmpty());
        verify(stallMapper).find(query);
    }

    @Test
    void addDefaultsCurrentStatusToZeroWhenMissing() {
        Stall stall = createStall();
        stall.setCurrentStatus(null);

        stallService.add(stall);

        assertEquals(0, stall.getCurrentStatus());
        verify(stallMapper).add(stall);
    }

    @Test
    void addCallsMapperOnceWhenStallIsValid() {
        Stall stall = createStall();
        stall.setCurrentStatus(1);

        stallService.add(stall);

        assertEquals(1, stall.getCurrentStatus());
        verify(stallMapper).add(stall);
    }

    @Test
    void deleteDoesNothingWhenIdsIsNull() {
        stallService.delete(null);

        verify(stallMapper, never()).delete(anyList());
        verify(dishMapper, never()).deleteByStallId(anyList());
    }

    @Test
    void deleteDoesNothingWhenIdsIsEmpty() {
        stallService.delete(Collections.emptyList());

        verify(stallMapper, never()).delete(anyList());
        verify(dishMapper, never()).deleteByStallId(anyList());
    }

    @Test
    void deleteCallsBothMappersWhenIdsPresent() {
        List<Integer> ids = List.of(1, 2);

        stallService.delete(ids);

        verify(stallMapper).delete(ids);
        verify(dishMapper).deleteByStallId(ids);
    }

    @Test
    void updateThrowsWhenStallIsNull() {
        assertThrows(IllegalArgumentException.class, () -> stallService.update(null));
        verify(stallMapper, never()).update(any(Stall.class));
    }

    @Test
    void updateThrowsWhenIdIsNull() {
        Stall stall = createStall();

        assertThrows(IllegalArgumentException.class, () -> stallService.update(stall));
        verify(stallMapper, never()).update(any(Stall.class));
    }

    @Test
    void updateThrowsWhenStallDoesNotExist() {
        Stall stall = createStall();
        stall.setId(1);
        when(stallMapper.find(any(Stall.class))).thenReturn(Collections.emptyList());

        assertThrows(StallNotExistException.class, () -> stallService.update(stall));
        verify(stallMapper, never()).update(any(Stall.class));
    }

    @Test
    void updateCallsMapperWhenStallExists() {
        Stall stall = createStall();
        stall.setId(1);
        when(stallMapper.find(any(Stall.class))).thenReturn(List.of(createStall()));

        stallService.update(stall);

        verify(stallMapper).update(stall);
    }

    private Stall createStall() {
        Stall stall = new Stall();
        stall.setName("烤冷面");
        stall.setNoonLocation("东区");
        stall.setEveningLocation("西区");
        stall.setNoonStartTime(LocalTime.of(11, 0));
        stall.setNoonEndTime(LocalTime.of(13, 0));
        stall.setEveningStartTime(LocalTime.of(17, 0));
        stall.setEveningEndTime(LocalTime.of(20, 0));
        return stall;
    }
}
