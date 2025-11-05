package pt.edequinox.controllers;

import pt.edequinox.models.OperationRequest;
import pt.edequinox.models.OperationResult;
import pt.edequinox.models.OperationType;
import pt.edequinox.service.RequestResponseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
public class CalculatorController {

    private final RequestResponseService requestResponseService;

    public CalculatorController(RequestResponseService requestResponseService) {
        this.requestResponseService = requestResponseService;
    }

    private ResponseEntity<OperationResult> performOperation(OperationType type, String operand1Str, String operand2Str) {
        String requestId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", requestId);

        try {
            BigDecimal operand1 = new BigDecimal(operand1Str);
            BigDecimal operand2 = new BigDecimal(operand2Str);

            OperationRequest request = new OperationRequest(type, operand1, operand2);

            OperationResult result = requestResponseService.sendAndReceive(request, requestId);

            if(result.getError() != null) {
                return new ResponseEntity<>(result, headers, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(
                new OperationResult(requestId, "Invalid number format"), 
                headers, 
                HttpStatus.BAD_REQUEST);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseEntity<>(
                new OperationResult(requestId, "Error processing request: " + e.getMessage()), 
                headers, 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/add")
    public ResponseEntity<OperationResult> add(@RequestParam String operand1, @RequestParam String operand2) {
        return performOperation(OperationType.ADDITION, operand1, operand2);
    }

    @GetMapping("/subtract")
    public ResponseEntity<OperationResult> subtract(@RequestParam String operand1, @RequestParam String operand2) {
        return performOperation(OperationType.SUBTRACTION, operand1, operand2);
    }

    @GetMapping("/multiply")
    public ResponseEntity<OperationResult> multiply(@RequestParam String operand1, @RequestParam String operand2) {
        return performOperation(OperationType.MULTIPLICATION, operand1, operand2);
    }

    @GetMapping("/divide")
    public ResponseEntity<OperationResult> divide(@RequestParam String operand1, @RequestParam String operand2) {
        return performOperation(OperationType.DIVISION, operand1, operand2);
    }
}
