package pt.edequinox.calculator.services;

import pt.edequinox.api.models.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResultProducer {

    public static final Logger logger = LoggerFactory.getLogger(ResultProducer.class);
    private final KafkaTemplate<String, OperationResult> kafkaTemplate;
    private final String resultTopic;

    public ResultProducer(KafkaTemplate<String, OperationResult> kafkaTemplate,
                          @org.springframework.beans.factory.annotation.Value("${app.kafka.topic.results}") String resultTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.resultTopic = resultTopic;
    }

    /**
     * Sends an operation result to the Kafka topic.
     * @param result   The operation result to be sent.
     * @param requestId The unique identifier for the request.
     */
    public void sendOperationResult(OperationResult result, String requestId) {
        logger.info("Sending operation result: {} to topic: {}", result, resultTopic);
        kafkaTemplate.send(resultTopic, requestId, result)
                .whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        logger.error("Failed to send operation result: {}", result, throwable);
                    } else {
                        logger.info("Operation result sent successfully with ID: {}", requestId);
                    }
                });
    }
}