package org.example.stall_manage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.stall_manage.mapper")
//相当于在每个mapper接口前加@Mapper
public class StallManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(StallManageApplication.class, args);
    }

}
