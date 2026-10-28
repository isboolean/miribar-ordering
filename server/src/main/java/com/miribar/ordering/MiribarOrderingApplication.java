package com.miribar.ordering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.miribar.ordering.mapper")
public class MiribarOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiribarOrderingApplication.class, args);
    }

}
