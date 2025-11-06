package pt.edequinox.calculator.components;

import pt.edequinox.api.models.OperationRequest;
import pt.edequinox.api.models.OperationResult;
import pt.edequinox.calculator.services.CalculatorService;
import pt.edequinox.calculator.services.ResultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;

import pt.edequinox.api.filters.FiltersContext;

@Component
public class OperationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OperationConsumer.class);
    private final CalculatorService calculatorService;
    private final ResultProducer resultProducer;

    public OperationConsumer(CalculatorService calculatorService, ResultProducer resultProducer) {
        this.calculatorService = calculatorService;
        this.resultProducer = resultProducer;
    }

    @KafkaListener(topics = "${app.kafka.topic.requests}", groupId = "calculator-group")
    public void handleOperationRequest(
        ConsumerRecord<String, OperationRequest> consumerRecord) {

            String requestId = consumerRecord.key();

        try {

            FiltersContext.put(requestId);

            OperationRequest request = consumerRecord.value();
            logger.info("Processing operation request: {}", request);

            BigDecimal result = calculatorService.performOperation(request);
            logger.info("Operation result for request ID {}: {}", requestId, result);

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
        } finally {
            FiltersContext.remove();
        }
    }
}