package com.distributed.foodsystem.orderintake.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String customerId;
    private List<OrderItem> items;
    private String storeId;
    private String deliveryAddress;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class OrderItem {
    private String sku;
    private int quantity;
    private double price;
}

