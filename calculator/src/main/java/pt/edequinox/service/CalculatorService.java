package pt.edequinox.service;

import pt.edequinox.models.OperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculatorService {

    public static final Logger logger = LoggerFactory.getLogger(CalculatorService.class);

    public BigDecimal performOperation(OperationRequest request) {

        BigDecimal result;
        BigDecimal operand1 = request.getOperand1();
        BigDecimal operand2 = request.getOperand2();

        logger.info("Performing operation: {}", request);
        switch (request.getOperationType()) {
            case ADDITION:
                result = operand1.add(operand2);
                break;
            case SUBTRACTION:
                result = operand1.subtract(operand2);
                break;
            case MULTIPLICATION:
                result = operand1.multiply(operand2);
                break;
            case DIVISION:
                if(operand2.compareTo(BigDecimal.ZERO) == 0) {
                    logger.error("Division by zero attempted: {}", request);
                    throw new ArithmeticException("Division by zero is not allowed.");
                }
                result = operand1.divide(operand2, RoundingMode.HALF_UP);
                break;
            default:
                logger.error("Invalid operation type: {}", request.getOperationType());
                throw new IllegalArgumentException("Invalid operation type");
        }
        logger.info("Operation result: {}", result);
        return result;
    }

}
