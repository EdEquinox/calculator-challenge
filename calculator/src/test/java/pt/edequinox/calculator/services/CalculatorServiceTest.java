package pt.edequinox.calculator.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.edequinox.api.models.OperationRequest;
import pt.edequinox.api.models.OperationType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService();
    }

    @Test
    void addition_ReturnsCorrectSum() {
        OperationRequest req = new OperationRequest(OperationType.ADDITION,
                new BigDecimal("2"), new BigDecimal("3"));

        BigDecimal result = calculatorService.performOperation(req);

        assertEquals(0, result.compareTo(new BigDecimal("5")));
    }

    @Test
    void subtraction_ReturnsCorrectDifference() {
        OperationRequest req = new OperationRequest(OperationType.SUBTRACTION,
                new BigDecimal("10"), new BigDecimal("4"));

        BigDecimal result = calculatorService.performOperation(req);

        assertEquals(0, result.compareTo(new BigDecimal("6")));
    }

    @Test
    void multiplication_ReturnsCorrectProduct() {
        OperationRequest req = new OperationRequest(OperationType.MULTIPLICATION,
                new BigDecimal("6"), new BigDecimal("7"));

        BigDecimal result = calculatorService.performOperation(req);

        assertEquals(0, result.compareTo(new BigDecimal("42")));
    }

    @Test
    void division_ReturnsCorrectQuotient() {
        OperationRequest req = new OperationRequest(OperationType.DIVISION,
                new BigDecimal("5"), new BigDecimal("2"));

        BigDecimal result = calculatorService.performOperation(req);

        // 5 / 2 => 2.5
        assertEquals(0, result.compareTo(new BigDecimal("2.5")));
    }

    @Test
    void division_ByZero_ThrowsArithmeticException() {
        OperationRequest req = new OperationRequest(OperationType.DIVISION,
                new BigDecimal("1"), BigDecimal.ZERO);

        assertThrows(ArithmeticException.class, () -> calculatorService.performOperation(req));
    }

}
