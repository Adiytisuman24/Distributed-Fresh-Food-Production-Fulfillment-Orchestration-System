package com.distributed.foodsystem.sla.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SlaService {

    private final Map<String, LocalDateTime> activeDeadlines = new ConcurrentHashMap<>();

    public void startSlaTracking(String orderId, int minutesToDeadline) {
        LocalDateTime deadline = LocalDateTime.now().plusMinutes(minutesToDeadline);
        activeDeadlines.put(orderId, deadline);
        log.info("SLA Tracking started for Order {}. Deadline: {}", orderId, deadline);
    }

    @Scheduled(fixedRate = 5000)
    public void monitorSlas() {
        LocalDateTime now = LocalDateTime.now();
        activeDeadlines.forEach((orderId, deadline) -> {
            if (now.isAfter(deadline)) {
                log.error("CRITICAL SLA BREACH: Order {} is LATE!", orderId);
                // In production, emit event to Failure Controller to trigger mitigations
                activeDeadlines.remove(orderId);
            } else if (now.plusMinutes(5).isAfter(deadline)) {
                log.warn("SLA RISK: Order {} will breach in < 5 mins!", orderId);
            }
        });
    }

    public void completeOrder(String orderId) {
        log.info("SLA successfully met for Order {}.", orderId);
        activeDeadlines.remove(orderId);
    }
}

