package com.demo.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.demo.order.adapter.out.product")
public class SqsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqsDemoApplication.class, args);
    }
}
