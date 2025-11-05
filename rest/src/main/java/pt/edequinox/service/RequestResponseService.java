package pt.edequinox.service;

import pt.edequinox.models.OperationRequest;
import pt.edequinox.models.OperationResult;
import pt.edequinox.producer.OperationProducer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class RequestResponseService {

    private final OperationProducer operationProducer;
    private final Map<String, CompletableFuture<OperationResult>> pendingRequests = new ConcurrentHashMap<>();

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    public RequestResponseService(OperationProducer operationProducer) {
        this.operationProducer = operationProducer;
    }

    public OperationResult sendAndReceive(OperationRequest request, String requestId)
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<OperationResult> futureResponse = new CompletableFuture<>();
        pendingRequests.put(requestId, futureResponse);

        try {
            operationProducer.sendOperationRequest(request, requestId);
            // wait synchronously for result or timeout
            return futureResponse.get(REQUEST_TIMEOUT.toSeconds(), SECONDS);
        } finally {
            pendingRequests.remove(requestId);
        }
    }

    public void completeRequest(String requestId, OperationResult result) {
        CompletableFuture<OperationResult> futureResponse = pendingRequests.get(requestId);
        if (futureResponse != null) {
            futureResponse.complete(result);
        }
    }

    public void completeRequestExceptionally(String requestId, Throwable ex) {
        CompletableFuture<OperationResult> futureResponse = pendingRequests.get(requestId);
        if (futureResponse != null) {
            futureResponse.completeExceptionally(ex);
        }
    }
}