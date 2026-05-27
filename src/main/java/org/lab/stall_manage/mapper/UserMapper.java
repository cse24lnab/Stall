package org.lab.stall_manage.mapper;

import org.lab.stall_manage.pojo.User;

public interface UserMapper {

    int add(User user);

    //不合并两个find,因为只会根据username或者id查表，分开语义更清楚
    User findByUsername(String username);

    User find(Integer id);

    int update(User user);
}
