package com.company.itoms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableCaching
@MapperScan("com.company.itoms.mapper")
public class ItomsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItomsApplication.class, args);
    }
}
