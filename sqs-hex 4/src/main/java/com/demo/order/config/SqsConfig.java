package com.demo.order.config;

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

/**
 * Configuração do SQS ajustada para ambiente com recursos limitados.
 *
 * Raciocínio das configurações para 1 vCPU / 2GB:
 *
 * maxMessagesPerPoll(10)
 *   → Limite máximo do protocolo SQS. Não aumentar.
 *
 * maxConcurrentMessages(20)
 *   → Controla quantas mensagens estão em processamento simultâneo.
 *   → Com Virtual Threads, o limite não é CPU (1 vCPU processa uma por vez)
 *     mas sim memória e pressão no I/O.
 *   → 20 VTs ativas ≈ 20 chamadas Kafka em voo + overhead de objetos em heap.
 *   → Aumentar para 50+ só se monitoramento mostrar CPU ociosa e throughput baixo.
 *
 * pollTimeout(20s)
 *   → Long-polling: o SQS segura a conexão por até 20s esperando mensagens.
 *   → Reduz chamadas vazias e custo de rede — importante com poucos recursos.
 *
 * acknowledgementMode(ON_SUCCESS)
 *   → ACK automático após processBatch() retornar sem exceção.
 *   → processBatch() só retorna após todos os ACKs do Kafka (allOf).
 *   → Garante: Kafka confirmou → SQS faz ACK. Zero risco de perda.
 */
@Configuration
public class SqsConfig {

    @Value("${sqs.listener.max-messages-per-poll}")
    private int maxMessagesPerPoll;

    @Value("${sqs.listener.max-concurrent-messages}")
    private int maxConcurrentMessages;

    @Value("${sqs.listener.poll-timeout}")
    private int pollTimeoutSeconds;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .region(Region.SA_EAST_1)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("test", "test")
                )
            )
            .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
        SqsAsyncClient sqsAsyncClient
    ) {
        return SqsMessageListenerContainerFactory
            .builder()
            .sqsAsyncClient(sqsAsyncClient)
            .configure(options -> options
                .listenerMode(ListenerMode.BATCH)
                .maxMessagesPerPoll(maxMessagesPerPoll)
                .maxConcurrentMessages(maxConcurrentMessages)
                .pollTimeout(Duration.ofSeconds(pollTimeoutSeconds))
                .acknowledgementMode(AcknowledgementMode.MANUAL)
                .acknowledgementOrdering(AcknowledgementOrdering.ORDERED)
            )
            .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }
}
