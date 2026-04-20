package org.example.stall_manage;

import org.example.stall_manage.mapper.StallMapper;
import org.example.stall_manage.pojo.Stall;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest
class StallManageApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
    void testDataSource() throws SQLException {

        System.out.println("数据源"+dataSource.getClass());

        Connection connection = dataSource.getConnection();
        System.out.println("连接"+connection);

        if(connection!=null){
            System.out.println("恭喜");
            connection.close();
        }
    }

    @Test
    void contextLoads() {
    }



}
