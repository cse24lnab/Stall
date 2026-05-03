package org.example.stall_manage;

import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Stall;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class StallMapperTest {

    @Autowired
    private StallMapper stallMapper;

    @Test
    void findById() {
        Stall query = new Stall();
        query.setId(1);

        List<Stall> stalls = stallMapper.find(query);

        assertEquals(1, stalls.size());
        assertEquals("烤冷面", stalls.get(0).getName());
        assertEquals(1, stalls.get(0).getCurrentStatus());
    }

    @Test
    void findByNameMapsUnderscoreFields() {
        Stall query = new Stall();
        query.setName("煎饼");

        List<Stall> stalls = stallMapper.find(query);

        assertEquals(1, stalls.size());
        Stall stall = stalls.get(0);
        assertEquals(2, stall.getId());
        assertEquals(0, stall.getCurrentStatus());
    }

    @Test
    void findWithEmptyCriteriaReturnsAllRows() {
        List<Stall> stalls = stallMapper.find(new Stall());

        assertEquals(2, stalls.size());
    }

    @Test
    void addAssignsGeneratedId() {
        Stall stall = new Stall();
        stall.setName("测试小摊");
        stall.setCurrentStatus(1);
        stall.setNoonLocation("南区");
        stall.setEveningLocation("北区");
        stall.setNoonStartTime(LocalTime.of(10, 30));
        stall.setNoonEndTime(LocalTime.of(13, 30));
        stall.setEveningStartTime(LocalTime.of(17, 30));
        stall.setEveningEndTime(LocalTime.of(21, 0));

        stallMapper.add(stall);

        assertNotNull(stall.getId());

        Stall query = new Stall();
        query.setName("测试小摊");
        List<Stall> inserted = stallMapper.find(query);

        assertEquals(1, inserted.size());
        assertEquals(stall.getId(), inserted.get(0).getId());
        assertEquals(1, inserted.get(0).getCurrentStatus());
    }
}
