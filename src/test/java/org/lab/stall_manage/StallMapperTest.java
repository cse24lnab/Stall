package org.lab.stall_manage;

import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Stall;
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

    @Test
    void deleteOneStall()
    {
        stallMapper.delete(List.of(1));
        List<Stall> existStall=stallMapper.find(new Stall());
        //把菜品删除是在service层用事务管理
        assertEquals(1, existStall.size());
        assertEquals(0,existStall.get(0).getIsDelete());
    }

    @Test
    void deleteMultiStall()
    {
        stallMapper.delete(List.of(1,2));
        List<Stall> existStall=stallMapper.find(new Stall());
        assertEquals(0, existStall.size());
    }

    @Test
    void updateExistStallNameAndNullNoonLocation()
    {
        Stall stall=new Stall();
        stall.setId(1);
        stall.setName("测试更新");
        stall.setNoonLocation("");
        stallMapper.update(stall);
        List<Stall> updateStall=stallMapper.find(stall);
        assertEquals("测试更新",updateStall.get(0).getName());
        assertEquals("东区",updateStall.get(0).getNoonLocation());
        assertEquals(0,updateStall.get(0).getIsDelete());
    }

    @Test
    void updateExistStallNoonLocationAndNullName()
    {
        Stall stall=new Stall();
        stall.setId(1);
        stall.setName("");
        stall.setNoonLocation("测试更新");
        stallMapper.update(stall);
        List<Stall> updateStall=stallMapper.find(stall);
        assertEquals("测试更新",updateStall.get(0).getNoonLocation());
        assertEquals("烤冷面",updateStall.get(0).getName());
        assertEquals(0,updateStall.get(0).getIsDelete());
    }

    @Test
    void updateNotExistStallNoChange()
    {
        stallMapper.delete(List.of(1));
        Stall stall=new Stall();
        stall.setId(1);
        stall.setNoonLocation("测试更新");
        int hasUpdate=stallMapper.update(stall);
        assertEquals(0,hasUpdate);
    }
}
