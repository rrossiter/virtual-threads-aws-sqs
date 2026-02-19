package com.demo.sqs;

import com.demo.sqs.model.OrderEvent;
import com.demo.sqs.producer.OrderProducer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SqsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqsDemoApplication.class, args);
    }

    /**
     * Envia 10 mensagens para demonstrar o consumo em batch.
     * O consumer receberá todas de uma vez (até maxMessagesPerPoll=10).
     */
//    @Bean
    public ApplicationRunner runner(OrderProducer producer) {
        return args -> {
            producer.send(OrderEvent.of("Notebook Dell",       2,  8500.00));
            producer.send(OrderEvent.of("Mouse Logitech",      5,   350.00));
            producer.send(OrderEvent.of("Teclado Mecânico",    1,   620.00));
            producer.send(OrderEvent.of("Monitor LG 27\"",     1,  2100.00));
            producer.send(OrderEvent.of("Headset Sony",        3,   890.00));
            producer.send(OrderEvent.of("Webcam Logitech",     2,   480.00));
            producer.send(OrderEvent.of("SSD Samsung 1TB",     4,   750.00));
            producer.send(OrderEvent.of("Memória RAM 32GB",    2,   640.00));
            producer.send(OrderEvent.of("Hub USB-C",           6,   210.00));
            producer.send(OrderEvent.of("Suporte Notebook",    3,   190.00));
        };
    }
}
