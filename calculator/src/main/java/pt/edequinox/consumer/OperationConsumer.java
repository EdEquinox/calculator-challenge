package pt.edequinox.consumer;

import pt.edequinox.models.OperationRequest;
import pt.edequinox.service.CalculatorService;
import pt.edequinox.models.OperationResult;
import pt.edequinox.producer.ResultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OperationConsumer {

    public static final Logger logger = LoggerFactory.getLogger(OperationConsumer.class);
    private final CalculatorService calculatorService;
    private final ResultProducer resultProducer;

    public OperationConsumer(CalculatorService calculatorService, ResultProducer resultProducer) {
        this.calculatorService = calculatorService;
        this.resultProducer = resultProducer;
    }

    @KafkaListener(topics = "${app.kafka.topic.requests}", groupId = "calculator-group")
    public void handleOperationRequest(
        OperationRequest request, 
        @Header("kafka_receivedMessageKey") String requestId) {
        try {
            logger.info("Received operation request: {}", request);
            BigDecimal result = calculatorService.performOperation(request);

            OperationResult operationResult = new OperationResult(requestId, request.getOperationType(), result);

            logger.info("Calculated result: {}", result);
            resultProducer.sendOperationResult(operationResult, requestId);
        } catch (ArithmeticException e) {
            logger.error("Error performing operation for request ID {}: {}", requestId, e.getMessage());
            OperationResult errorResult = new OperationResult(requestId, e.getMessage());
            resultProducer.sendOperationResult(errorResult, requestId);
        } catch (Exception e) {
            logger.error("Unexpected error for request ID {}: {}", requestId, e.getMessage());
            OperationResult errorResult = new OperationResult(requestId, e.getMessage());
            resultProducer.sendOperationResult(errorResult, requestId);
        }
    }
}