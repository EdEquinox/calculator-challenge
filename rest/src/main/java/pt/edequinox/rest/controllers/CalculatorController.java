package pt.edequinox.rest.controllers;

import pt.edequinox.api.filters.FiltersContext;
import pt.edequinox.api.models.OperationRequest;
import pt.edequinox.api.models.OperationResult;
import pt.edequinox.api.models.OperationType;
import pt.edequinox.rest.services.RequestResponseService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


@RestController
public class CalculatorController {

    private final RequestResponseService requestResponseService;

    public CalculatorController(RequestResponseService requestResponseService) {
        this.requestResponseService = requestResponseService;
    }

    private ResponseEntity<OperationResult> performOperation(OperationType type, String operand1Str, String operand2Str) {

        String requestId = FiltersContext.get();
        if (requestId == null) {
            return new ResponseEntity<>(
                new OperationResult(null, "Missing request id (request filter not applied)"),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // `RequestIdFilter` already sets the `X-Request-ID` response header.
        // Do not set it again here to avoid duplicate headers.

        try {
            BigDecimal operand1 = new BigDecimal(operand1Str);
            BigDecimal operand2 = new BigDecimal(operand2Str);

            OperationRequest request = new OperationRequest(type, operand1, operand2);

            OperationResult result = requestResponseService.sendAndReceive(request, requestId);

            if(result.getError() != null) {
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(
                new OperationResult(requestId, "Invalid number format"), 
                HttpStatus.BAD_REQUEST);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseEntity<>(
                new OperationResult(requestId, "Error processing request: " + e.getMessage()), 
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
