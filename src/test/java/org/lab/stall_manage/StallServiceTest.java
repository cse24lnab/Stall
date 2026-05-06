package org.lab.stall_manage;

import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.impl.StallServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StallServiceTest {
    @Mock
    private StallMapper stallMapper;

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
