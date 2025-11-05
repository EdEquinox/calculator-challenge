package pt.edequinox.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OperationResult {
    private String requestId;
    private OperationType operationType;
    private BigDecimal result;
    private String error;

    // Convenience constructor for successful result
    public OperationResult(String requestId, OperationType operationType, BigDecimal result) {
        this.requestId = requestId;
        this.operationType = operationType;
        this.result = result;
        this.error = null;
    }

    // Convenience constructor for error result
    public OperationResult(String requestId, String error) {
        this.requestId = requestId;
        this.error = error;
    }
}