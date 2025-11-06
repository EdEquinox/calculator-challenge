package pt.edequinox.api.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OperationRequest {
    private OperationType operationType;
    private BigDecimal operand1;
    private BigDecimal operand2;
}
