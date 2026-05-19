package com.datafactory.script;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.datafactory.script.mapper")
public class ScriptApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScriptApplication.class, args);
    }
}
