package pt.edequinox.producer;

import pt.edequinox.models.OperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OperationProducer {

    public static final Logger logger = LoggerFactory.getLogger(OperationProducer.class);
    private final KafkaTemplate<String, OperationRequest> kafkaTemplate;
    private final String requestTopic;

    public OperationProducer(KafkaTemplate<String, OperationRequest> kafkaTemplate, 
    @org.springframework.beans.factory.annotation.Value("${app.kafka.topic.requests}") String requestTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestTopic = requestTopic;
    }

    /**
     * Sends an operation request to the Kafka topic.
     * @param request   The operation request to be sent.
     * @param requestId The unique identifier for the request.
     */
    public CompletableFuture<Void> sendOperationRequest(OperationRequest request, String requestId) {
        logger.info("Sending operation request: {} to topic: {}", request, requestTopic);
        return kafkaTemplate.send(requestTopic, requestId, request)
                .thenAccept(result -> logger.info("Operation request sent successfully with ID: {}", requestId))
                .exceptionally(ex -> {
                    logger.error("Failed to send operation request: {}", request, ex);
                    return null;
                });
    }
}