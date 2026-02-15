package com.distributed.foodsystem.audit.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AuditService {

    private final Map<String, List<AuditEntry>> auditTrail = new ConcurrentHashMap<>();

    public void logAudit(String entityId, String action, String actor, String details) {
        AuditEntry entry = AuditEntry.builder()
                .timestamp(LocalDateTime.now())
                .action(action)
                .actor(actor)
                .details(details)
                .build();

        auditTrail.computeIfAbsent(entityId, k -> new ArrayList<>()).add(entry);
        log.info("AUDIT LOG: [{}] {} by {} - {}", entityId, action, actor, details);
    }

    public List<AuditEntry> getTraceability(String entityId) {
        return auditTrail.getOrDefault(entityId, new ArrayList<>());
    }

    @Data
    @Builder
    public static class AuditEntry {
        private LocalDateTime timestamp;
        private String action;
        private String actor; // Machine ID or Staff ID
        private String details;
    }
}

