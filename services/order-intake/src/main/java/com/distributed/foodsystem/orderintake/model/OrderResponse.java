package com.distributed.foodsystem.orderintake.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String status;
    private String idempotencyKey;
    private LocalDateTime timestamp;
    private String estimatedPrepTime;
}

