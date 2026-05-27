package org.lab.stall_manage;

import org.junit.jupiter.api.Test;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/*
测试动态sql
 */
@SpringBootTest
@Transactional
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void updateNickNameAndNullPhone() {
        User user = new User();
        user.setNickname("test");
        user.setPhone("");
        user.setId(1);
        int update = userMapper.update(user);
        User user1 = userMapper.find(1);
        assertEquals(1,update);
        assertEquals("test",user1.getNickname());
        assertNull(user1.getPhone());
    }

    @Test
    void updatePasswordHashAndPhone()
    {
        User user = new User();
        user.setPhone("test");
        user.setPasswordHash("testpwd");
        user.setId(1);
        int update = userMapper.update(user);
        User user1 = userMapper.find(1);
        assertEquals(1,update);
        assertEquals("test",user1.getPhone());
        assertEquals("testpwd",user1.getPasswordHash());
    }

}
