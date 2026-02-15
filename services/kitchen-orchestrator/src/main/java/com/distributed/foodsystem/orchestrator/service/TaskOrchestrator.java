package com.distributed.foodsystem.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TaskOrchestrator {

    // Task ID -> Status (e.g., ASSIGNED, IN_PROGRESS, COMPLETED)
    private final Map<String, String> taskRegistry = new ConcurrentHashMap<>();

    public void startTask(String taskId, String workerId) {
        log.info("Attempting to start task: {} by worker: {}", taskId, workerId);

        // ATOMIC CHECK-AND-SET: Exactly-Once Execution
        String currentStatus = taskRegistry.putIfAbsent(taskId, "IN_PROGRESS");

        if (currentStatus != null) {
            log.warn("TASK DUPLICATION PREVENTED: Task {} is already {}. Retry rejected.", taskId, currentStatus);
            return;
        }

        log.info("Task {} successfully claimed. Commencing physical production logic...", taskId);
        // Physical logic (Simulated)
    }

    public void completeTask(String taskId) {
        if ("IN_PROGRESS".equals(taskRegistry.get(taskId))) {
            taskRegistry.put(taskId, "COMPLETED");
            log.info("Task {} completed. Updating global SLA tracking.", taskId);
        }
    }
}

