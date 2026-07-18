package org.lab.stall_manage;

import org.junit.jupiter.api.Test;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findByUsernameReturnsUserWhenExists() {
        User user = userMapper.findByUsername("user_demo");

        assertNotNull(user);
        assertEquals("user_demo", user.getUsername());
        assertEquals("普通用户演示账号", user.getNickname());
        assertEquals(0, user.getRole().getCode());
    }

    @Test
    void findByIdReturnsUserWhenExists() {
        User user = userMapper.find(1);

        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("user_demo", user.getUsername());
    }

    @Test
    void addAssignsGeneratedId() {
        User user = new User();
        user.setUsername("new_user");
        user.setPasswordHash("hash");
        user.setNickname("新用户");
        user.setPhone("13800000009");

        int inserted = userMapper.add(user);

        assertEquals(1, inserted);
        assertNotNull(user.getId());

        User saved = userMapper.find(user.getId());
        assertNotNull(saved);
        assertEquals("new_user", saved.getUsername());
        assertEquals("新用户", saved.getNickname());
        assertEquals("13800000009", saved.getPhone());
        assertEquals(0, saved.getIsDelete());
    }

    @Test
    void updateNickNameAndBlankPhoneIgnoresPhone() {
        User user = new User();
        user.setId(1);
        user.setNickname("test");
        user.setPhone("");

        int update = userMapper.update(user);
        User updated = userMapper.find(1);

        assertEquals(1, update);
        assertEquals("test", updated.getNickname());
        assertNull(updated.getPhone());
    }

    @Test
    void updatePasswordHashAndPhone() {
        User user = new User();
        user.setId(1);
        user.setPhone("test");
        user.setPasswordHash("testpwd");

        int update = userMapper.update(user);
        User updated = userMapper.find(1);

        assertEquals(1, update);
        assertEquals("test", updated.getPhone());
        assertEquals("testpwd", updated.getPasswordHash());
        assertEquals("普通用户演示账号", updated.getNickname());
    }

    @Test
    void updateAvatarFileIdOnlyKeepsOtherFields() {
        User user = new User();
        user.setId(1);
        user.setAvatarFileId(22);

        int update = userMapper.update(user);
        User updated = userMapper.find(1);

        assertEquals(1, update);
        assertEquals(22, updated.getAvatarFileId());
        assertEquals("user_demo", updated.getUsername());
        assertEquals("普通用户演示账号", updated.getNickname());
    }

    @Test
    void updateAvatarUrlOnlyKeepsOtherFields() {
        User user = new User();
        user.setId(1);
        user.setAvatarUrl("https://example.com/avatar.png");

        int update = userMapper.update(user);
        User updated = userMapper.find(1);

        assertEquals(1, update);
        assertEquals("https://example.com/avatar.png", updated.getAvatarUrl());
        assertEquals("user_demo", updated.getUsername());
        assertEquals("普通用户演示账号", updated.getNickname());
    }

    @Test
    void findReturnsNullWhenUserIsDeleted() {
        jdbcTemplate.update("UPDATE `user` SET is_delete = 1 WHERE id = ?", 1);

        assertNull(userMapper.find(1));
        assertNull(userMapper.findByUsername("user_demo"));
    }

    @Test
    void updateReturnsZeroWhenUserIsDeleted() {
        jdbcTemplate.update("UPDATE `user` SET is_delete = 1 WHERE id = ?", 1);

        User user = new User();
        user.setId(1);
        user.setNickname("should_not_update");

        int update = userMapper.update(user);

        assertEquals(0, update);
    }
}
