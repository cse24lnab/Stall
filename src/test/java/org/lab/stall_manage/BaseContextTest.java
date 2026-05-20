package org.lab.stall_manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.pojo.enums.UserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BaseContextTest {

    @AfterEach
    void tearDown() {
        BaseContext.RemoveCurrentUser();
    }

    @Test
    void setCurrentUserThenGetCurrentUserReturnsSameContent() {
        CurrentUser currentUser = new CurrentUser(1, "alice01", UserRole.ADMIN);

        BaseContext.setCurrentUser(currentUser);

        CurrentUser stored = BaseContext.getCurrentUser();
        assertEquals(1, stored.getId());
        assertEquals("alice01", stored.getUsername());
        assertEquals(UserRole.ADMIN, stored.getRole());
    }

    @Test
    void removeCurrentUserClearsThreadLocal() {
        BaseContext.setCurrentUser(new CurrentUser(1, "alice01", UserRole.USER));

        BaseContext.RemoveCurrentUser();

        assertNull(BaseContext.getCurrentUser());
    }
}
