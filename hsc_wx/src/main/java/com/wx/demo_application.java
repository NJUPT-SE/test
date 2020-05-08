package com.wx;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages= "com.wx")//添加扫包
@EnableAutoConfiguration
public class demo_application {

    public static void main(String[] args) {
        SpringApplication.run(demo_application.class, args);
    }

}
