package org.lab.stall_manage;

import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.pojo.Stall;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class StallMapperTest {

    @Autowired
    private StallMapper stallMapper;

    @Test
    void findById() {
        Stall stall = stallMapper.findById(1);

        assertNotNull(stall);
        assertEquals("烤冷面", stall.getName());
        assertEquals(1, stall.getCurrentStatus());
        assertEquals(2, stall.getOwnerUserId());
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
    void findSupportsPartialName() {
        Stall query = new Stall();
        query.setName("冷面");

        List<Stall> stalls = stallMapper.find(query);

        assertEquals(1, stalls.size());
        assertEquals(1, stalls.get(0).getId());
    }

    @Test
    void findSupportsCurrentStatus() {
        Stall query = new Stall();
        query.setCurrentStatus(0);

        List<Stall> stalls = stallMapper.find(query);

        assertEquals(1, stalls.size());
        assertEquals(2, stalls.get(0).getId());
    }

    @Test
    void findCombinesOwnerNameAndStatus() {
        Stall query = new Stall();
        query.setOwnerUserId(4);
        query.setName("煎");
        query.setCurrentStatus(0);

        List<Stall> stalls = stallMapper.find(query);

        assertEquals(1, stalls.size());
        assertEquals(2, stalls.get(0).getId());
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
        stall.setOwnerUserId(2);

        int inserted = stallMapper.add(stall);

        assertEquals(1, inserted);
        assertNotNull(stall.getId());

        Stall query = new Stall();
        query.setName("测试小摊");
        List<Stall> insertedStalls = stallMapper.find(query);

        assertEquals(1, insertedStalls.size());
        assertEquals(stall.getId(), insertedStalls.get(0).getId());
        assertEquals(1, insertedStalls.get(0).getCurrentStatus());
        assertEquals(2, insertedStalls.get(0).getOwnerUserId());
    }

    @Test
    void activeStallNameMustBeUnique() {
        Stall duplicate = new Stall();
        duplicate.setName("烤冷面");
        duplicate.setCurrentStatus(0);
        duplicate.setOwnerUserId(2);

        assertThrows(DataIntegrityViolationException.class, () -> stallMapper.add(duplicate));
    }

    @Test
    void deletedStallNameCanBeReusedRepeatedly() {
        assertEquals(1, stallMapper.delete(List.of(1)));

        Stall replacement = new Stall();
        replacement.setName("烤冷面");
        replacement.setCurrentStatus(0);
        replacement.setOwnerUserId(2);
        assertEquals(1, stallMapper.add(replacement));

        assertEquals(1, stallMapper.delete(List.of(replacement.getId())));

        Stall secondReplacement = new Stall();
        secondReplacement.setName("烤冷面");
        secondReplacement.setCurrentStatus(1);
        secondReplacement.setOwnerUserId(2);
        assertEquals(1, stallMapper.add(secondReplacement));

        Stall query = new Stall();
        query.setName("烤冷面");
        List<Stall> activeStalls = stallMapper.find(query);
        assertEquals(1, activeStalls.size());
        assertEquals(secondReplacement.getId(), activeStalls.get(0).getId());
    }

    @Test
    void deleteOneStall()
    {
        int deleted = stallMapper.delete(List.of(1));

        Stall deletedQuery = new Stall();
        deletedQuery.setId(1);
        Stall remainQuery = new Stall();
        remainQuery.setId(2);

        assertEquals(1, deleted);
        assertEquals(0, stallMapper.find(deletedQuery).size());
        assertEquals(1, stallMapper.find(remainQuery).size());
        assertEquals("煎饼", stallMapper.find(remainQuery).get(0).getName());
    }

    @Test
    void deleteMultiStall()
    {
        int deleted = stallMapper.delete(List.of(1,2));

        assertEquals(2, deleted);
        assertEquals(0, stallMapper.find(new Stall()).size());
    }

    @Test
    void updateExistStallNameAndNullNoonLocation()
    {
        Stall stall=new Stall();
        stall.setId(1);
        stall.setName("测试更新");
        stall.setNoonLocation("");
        int updated = stallMapper.update(stall);
        List<Stall> updateStall=stallMapper.find(stall);
        assertEquals(1, updated);
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
        int updated = stallMapper.update(stall);
        List<Stall> updateStall=stallMapper.find(stall);
        assertEquals(1, updated);
        assertEquals("测试更新",updateStall.get(0).getNoonLocation());
        assertEquals("烤冷面",updateStall.get(0).getName());
        assertEquals(0,updateStall.get(0).getIsDelete());
    }

    @Test
    void updateCurrentStatusAndEveningEndTime()
    {
        Stall stall = new Stall();
        stall.setId(1);
        stall.setCurrentStatus(0);
        stall.setEveningEndTime(LocalTime.of(22, 30));

        int updated = stallMapper.update(stall);

        Stall query = new Stall();
        query.setId(1);
        List<Stall> updatedStalls = stallMapper.find(query);

        assertEquals(1, updated);
        assertEquals(1, updatedStalls.size());
        assertEquals(0, updatedStalls.get(0).getCurrentStatus());
        assertEquals(LocalTime.of(22, 30), updatedStalls.get(0).getEveningEndTime());
        assertEquals("烤冷面", updatedStalls.get(0).getName());
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
