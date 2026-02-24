package com.demo.sqs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SqsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqsDemoApplication.class, args);
    }
}
