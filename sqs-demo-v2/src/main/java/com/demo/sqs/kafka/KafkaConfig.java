package com.demo.sqs.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.orders}")
    private String ordersTopic;

    /**
     * Cria o tópico automaticamente se não existir.
     * partitions(3) — paralelismo de consumo
     * replicas(1)   — ambiente local, sem replicação
     */
    @Bean
    public NewTopic ordersReceivedTopic() {
        return TopicBuilder.name(ordersTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
