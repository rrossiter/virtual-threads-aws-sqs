package com.demo.sqs.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.ListenerMode;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;
import java.time.Duration;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.sqs.endpoint}")
    private String sqsEndpoint;

    // -------------------------------------------------------------------------
    // SqsAsyncClient apontando para o LocalStack
    // -------------------------------------------------------------------------
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .region(Region.SA_EAST_1)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("teste", "teste")
                )
            )
            .build();
    }

    // -------------------------------------------------------------------------
    // Factory com configurações de batch
    //
    // listenerMode(BATCH)      → entrega lista de mensagens ao consumer
    // maxMessagesPerPoll(10)   → máximo de mensagens por requisição ao SQS (max SQS = 10)
    // maxConcurrentMessages(20)→ quantas mensagens podem estar em processamento simultâneo
    // pollTimeout(20s)         → tempo máximo aguardando mensagens no long-polling
    // acknowledgementMode      → ON_SUCCESS: confirma (deleta da fila) só se não lançar exceção
    // acknowledgementOrdering  → ORDERED: garante ack na ordem recebida
    // -------------------------------------------------------------------------
    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
        SqsAsyncClient sqsAsyncClient
    ) {
        return SqsMessageListenerContainerFactory
            .builder()
            .sqsAsyncClient(sqsAsyncClient)
            .configure(options -> options
                .listenerMode(ListenerMode.BATCH)
                .maxMessagesPerPoll(10)
                .maxConcurrentMessages(20)
//                .pollTimeout(Duration.ofSeconds(20))
                .acknowledgementMode(AcknowledgementMode.ON_SUCCESS)
                .acknowledgementOrdering(AcknowledgementOrdering.ORDERED)
            )
            .build();
    }

    // -------------------------------------------------------------------------
    // SqsTemplate para envio de mensagens
    // -------------------------------------------------------------------------
//    @Bean
//    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
//        return SqsTemplate.newTemplate(sqsAsyncClient);
//    }
}
