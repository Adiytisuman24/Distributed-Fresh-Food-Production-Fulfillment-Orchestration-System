package com.distributed.foodsystem.planner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PlanningService {

    public List<String> decomposeOrderIntoTasks(String orderId, List<String> skus) {
        log.info("Planning production for order: {}", orderId);
        List<String> tasks = new ArrayList<>();

        for (String sku : skus) {
            tasks.add("PREP:" + sku + ":" + UUID.randomUUID().toString().substring(0, 4));
            tasks.add("COOK:" + sku + ":" + UUID.randomUUID().toString().substring(0, 4));
            tasks.add("PACK:" + sku + ":" + UUID.randomUUID().toString().substring(0, 4));
        }

        // Distributed Food System Principal: Reroute early if SLA risk is high
        String targetKitchen = determineOptimalKitchen(orderId);
        log.info("All {} tasks for order {} assigned to Kitchen {}", tasks.size(), orderId, targetKitchen);

        return tasks;
    }

    private String determineOptimalKitchen(String orderId) {
        // Mock logic: In real system, query Kitchen Health microservice
        double capacity = Math.random();
        if (capacity > 0.8) {
            log.warn("SLA RISK: Kitchen-A at {:.2f}% capacity. Rerouting to Kitchen-B.", capacity * 100);
            return "KITCHEN-B";
        }
        return "KITCHEN-A";
    }
}


