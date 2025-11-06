package pt.edequinox.rest.components;

import pt.edequinox.api.models.OperationResult;
import pt.edequinox.rest.services.RequestResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ResultConsumer {

    public static final Logger logger = LoggerFactory.getLogger(ResultConsumer.class);
    private final RequestResponseService requestResponseService;

    public ResultConsumer(RequestResponseService requestResponseService) {
        this.requestResponseService = requestResponseService;
    }

    @KafkaListener(topics = "${app.kafka.topic.results}", groupId = "rest-group")
    public void handleOperationResult(
        OperationResult result, 
        @Header("kafka_receivedMessageKey") String requestId) {
        logger.info("Received operation result: {}", result);
        
        try {
            if (result.getError() != null) {
                requestResponseService.completeRequestExceptionally(requestId, 
                    new Exception("Operation error: " + result.getError()));
            } else {
                requestResponseService.completeRequest(requestId, result);
            }
        } catch (Exception e) {
            logger.error("Error completing request for ID {}: {}", requestId, e.getMessage());
        }
    }
}