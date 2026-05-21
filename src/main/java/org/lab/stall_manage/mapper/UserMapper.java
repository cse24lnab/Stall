package org.lab.stall_manage.mapper;

import org.lab.stall_manage.pojo.User;

public interface UserMapper {

    public void add(User user);

    //不合并两个find,因为只会根据username或者id查表，分开语义更清楚
    public User findByUsername(String username);

    public User find(Integer id);

    public void update(User user);
}
