package org.lab.stall_manage.mapper;

import org.lab.stall_manage.pojo.User;

public interface UserMapper {

    public void add(User user);

    public User findByUsername(String username);
}
