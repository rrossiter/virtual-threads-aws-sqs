package com.demo.order.config;

import org.springframework.context.annotation.Configuration;

/**
 * Virtual Threads são gerenciadas pelo StructuredTaskScope no OrderService.
 * Não é necessário configurar ExecutorService ou Semaphore separadamente —
 * o escopo cuida do ciclo de vida das VTs e do throttle naturalmente
 * via maxConcurrentMessages no SqsConfig.
 */
@Configuration
public class VirtualThreadConfig {
    // Configuração feita via application.yml:
    // spring.threads.virtual.enabled=true
}
