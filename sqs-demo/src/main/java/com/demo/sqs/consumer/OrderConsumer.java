package com.demo.sqs.consumer;

import com.demo.sqs.model.OrderEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.BatchAcknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    /**
     * Recebe mensagens em batch da fila SQS.
     *
     * - List<Message<OrderEvent>>    → batch de mensagens com headers e payload
     * - BatchAcknowledgement<?>      → controle manual de acknowledgement do lote
     *
     * O factory configurado com AcknowledgementMode.ON_SUCCESS fará o ack
     * automático ao final, mas BatchAcknowledgement permite ack parcial ou
     * tratamento de falhas por mensagem individualmente.
     */


    @SqsListener(value = "${app.queue.name}", factory = "defaultSqsListenerContainerFactory")
    public void listen(
        List<Message<OrderEvent>> messages
          //  ,
        //BatchAcknowledgement<OrderEvent> acknowledgement
    ) {
        log.info("=== Batch recebido: {} mensagens ===", messages.size());

        List<Message<OrderEvent>> processadas = messages.stream()
            .filter(this::processar)
            .toList();

        // Faz ack apenas das mensagens processadas com sucesso
        if (!processadas.isEmpty()) {
            //acknowledgement.acknowledge(processadas);
            log.info("Ack enviado para {} mensagens.", processadas.size());
        }

        int falhas = messages.size() - processadas.size();
        if (falhas > 0) {
            log.warn("{} mensagens não foram confirmadas e voltarão à fila após visibility timeout.", falhas);
        }
    }

    private boolean processar(Message<OrderEvent> message) {
        try {
            OrderEvent event = message.getPayload();
            log.info("  → OrderId: {} | Produto: {} | Qtd: {} | Total: R$ {}",
                event.orderId(), event.product(), event.quantity(), event.totalPrice());
            return true;
        } catch (Exception ex) {
            log.error("Erro ao processar mensagem: {}", message.getHeaders().getId(), ex);
            return false;
        }
    }
}
