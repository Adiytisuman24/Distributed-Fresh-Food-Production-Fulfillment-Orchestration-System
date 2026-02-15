package com.distributed.foodsystem.orderintake.service;

import com.distributed.foodsystem.orderintake.model.OrderRequest;
import com.distributed.foodsystem.orderintake.model.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OrderService {

    // Simple in-memory idempotency store for simulation
    // In production, this would be DynamoDB with TTL
    private final Map<String, OrderResponse> idempotencyStore = new ConcurrentHashMap<>();
    
    // Mock order store
    private final Map<String, OrderResponse> orderStore = new ConcurrentHashMap<>();

    public OrderResponse processOrder(String idempotencyKey, OrderRequest request) {
        log.info("Processing order with idempotency key: {}", idempotencyKey);

        // 1. Exactly-Once execution via Idempotency Key
        if (idempotencyStore.containsKey(idempotencyKey)) {
            log.warn("Duplicate request detected for key: {}. Returning cached response.", idempotencyKey);
            return idempotencyStore.get(idempotencyKey);
        }

        // 2. SLA Feasibility Check (Backpressure simulation)
        if ("STORE_OVERLOADED".equals(request.getStoreId())) {
            throw new RuntimeException("SLA Breach Risk: Kitchen capacity exceeded. Order rejected.");
        }

        // 3. Create Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status("ACCEPTED")
                .idempotencyKey(idempotencyKey)
                .timestamp(LocalDateTime.now())
                .estimatedPrepTime("25 mins")
                .build();

        // 4. Persistence & Idempotency locking
        idempotencyStore.put(idempotencyKey, response);
        orderStore.put(orderId, response);

        log.info("Order {} accepted successfully.", orderId);
        
        // 5. Emit event to SQS (Mocked for now)
        emitOrderPlacedEvent(orderId, request);

        return response;
    }

    public OrderResponse getOrder(String orderId) {
        return orderStore.getOrDefault(orderId, null);
    }

    private void emitOrderPlacedEvent(String orderId, OrderRequest request) {
        // Logic to push to SQS for Production Planner to pick up
        log.info("Emitted ORDER_PLACED event for {}", orderId);
    }
}

