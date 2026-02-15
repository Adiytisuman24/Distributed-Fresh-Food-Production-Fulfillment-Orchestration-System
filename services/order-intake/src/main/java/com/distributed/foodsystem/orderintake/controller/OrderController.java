package com.distributed.foodsystem.orderintake.controller;

import com.distributed.foodsystem.orderintake.model.OrderRequest;
import com.distributed.foodsystem.orderintake.model.OrderResponse;
import com.distributed.foodsystem.orderintake.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody OrderRequest request) {
        
        OrderResponse response = orderService.processOrder(idempotencyKey, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
}

