package com.example.reusepos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.reusepos.mapper")
public class ReusePosApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReusePosApplication.class, args);
    }
}
