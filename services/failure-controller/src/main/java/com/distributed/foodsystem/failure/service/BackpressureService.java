package com.distributed.foodsystem.failure.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class BackpressureService {

    @Getter
    private boolean systemThrottled = false;

    @Getter
    private final Set<String> restrictedSkus = new HashSet<>();

    public void activateThrottling(String reason) {
        log.warn("CRITICAL: Activating system-wide backpressure. Reason: {}", reason);
        this.systemThrottled = true;
    }

    public void deactivateThrottling() {
        log.info("System health restored. Deactivating backpressure.");
        this.systemThrottled = false;
        this.restrictedSkus.clear();
    }

    public void restrictSku(String sku) {
        log.info("Restricting SKU: {} to preserve high-priority production.", sku);
        restrictedSkus.add(sku);
    }

    public double getCurrentCapacity() {
        // Mock capacity calculation
        return systemThrottled ? 0.3 : 0.95;
    }
}

