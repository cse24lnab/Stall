package org.lab.stall_manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.StallService;
import org.lab.stall_manage.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = StallManageApplication.class)
@Transactional
class StallServicePaginationIntegrationTest {

    @Autowired
    private StallService stallService;

    @BeforeEach
    void loginAsAdmin() {
        BaseContext.setCurrentUser(new CurrentUser(3, "admin_demo", UserRole.ADMIN));
    }

    @AfterEach
    void clearContext() {
        BaseContext.RemoveCurrentUser();
    }

    @Test
    void findReturnsFirstPage() {
        PageVO<Stall> page = stallService.find(1, 1, new Stall());

        assertEquals(2, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(1, page.getRecords().get(0).getId());
    }

    @Test
    void findReturnsSecondPage() {
        PageVO<Stall> page = stallService.find(2, 1, new Stall());

        assertEquals(2, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(2, page.getRecords().get(0).getId());
    }

    @Test
    void findCombinesPartialNameAndCurrentStatus() {
        Stall query = new Stall();
        query.setName("煎");
        query.setCurrentStatus(0);

        PageVO<Stall> page = stallService.find(1, 10, query);

        assertEquals(1, page.getTotal());
        assertEquals(2, page.getRecords().get(0).getId());
    }
}
