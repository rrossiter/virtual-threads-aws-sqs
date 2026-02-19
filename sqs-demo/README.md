# sqs-demo

Exemplo de integração **Spring Boot 3.5 + Java 21 + AWS SQS + LocalStack 4.0** com consumo em **batch** via `SqsMessageListenerContainerFactory`.

## Versões utilizadas

| Componente          | Versão  |
|---------------------|---------|
| Java                | 21      |
| Spring Boot         | 3.5.7   |
| Spring Cloud AWS    | 3.4.0   |
| LocalStack          | 4.0     |

## Arquitetura

```
OrderProducer  →  SQS (LocalStack)  →  SqsMessageListenerContainerFactory  →  OrderConsumer (batch)
```

## Configurações de batch (AwsConfig.java)

| Opção                    | Valor        | Descrição                                              |
|--------------------------|--------------|--------------------------------------------------------|
| `listenerMode`           | `BATCH`      | Entrega lista de mensagens ao consumer                 |
| `maxMessagesPerPoll`     | `10`         | Máximo por poll ao SQS (limite do SQS é 10)            |
| `maxConcurrentMessages`  | `20`         | Mensagens em processamento simultâneo                  |
| `pollTimeout`            | `20s`        | Long-polling: espera até 20s por mensagens             |
| `acknowledgementMode`    | `ON_SUCCESS` | Confirma (deleta da fila) somente se não houver exceção|
| `acknowledgementOrdering`| `ORDERED`    | Garante ack na ordem em que foram recebidas            |

## Como executar

### 1. Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker + Docker Compose

### 2. Subir o LocalStack
```bash
chmod +x init-localstack.sh
docker-compose up -d
```

### 3. Verificar se a fila foi criada
```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1
```

### 4. Executar a aplicação
```bash
mvn spring-boot:run
```

Ao iniciar, a aplicação envia **10 mensagens** e o consumer as recebe em **um único batch**.

## Estrutura do projeto

```
sqs-demo/
├── docker-compose.yml
├── init-localstack.sh
├── pom.xml
└── src/main/
    ├── java/com/demo/sqs/
    │   ├── SqsDemoApplication.java
    │   ├── config/AwsConfig.java          ← Factory + SqsAsyncClient + SqsTemplate
    │   ├── producer/OrderProducer.java
    │   ├── consumer/OrderConsumer.java    ← Consumo em batch com BatchAcknowledgement
    │   └── model/OrderEvent.java
    └── resources/application.yml
```
