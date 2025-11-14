package com.danya.workload;

import com.danya.workload.api.client.WorkloadFeignClient;
import com.danya.workload.api.dto.BatchUpdateWorkloadRequest;
import com.danya.workload.api.dto.CreateWorkloadRequest;
import com.danya.workload.api.dto.UpdateWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {
    private final WorkloadFeignClient workloadFeignClient;

    @CircuitBreaker(name = "workload-service")
    public void createWorkload(CreateWorkloadRequest payload) {
        log.info("Attempting to create workload for trainer with username '{}'", payload.username());
        workloadFeignClient.createWorkload(payload);
        log.info("Successfully created workload for trainer with username '{}'", payload.username());
    }

    @CircuitBreaker(name = "workload-service")
    public void addDuration(UpdateWorkloadRequest payload) {
        log.info("Attempting to add workload duration for trainer with username '{}'", payload.username());
        workloadFeignClient.addDuration(payload);
        log.info("Successfully added workload duration for trainer with username '{}'", payload.username());
    }

    @CircuitBreaker(name = "workload-service")
    public void subtractDuration(BatchUpdateWorkloadRequest payload) {
        Set<String> usernames = payload.updates().stream()
                .map(UpdateWorkloadRequest::username)
                .collect(Collectors.toSet());

        log.info("Attempting to subtract workload duration for trainers: {}", usernames);
        workloadFeignClient.subtractDuration(payload);
        log.info("Successfully subtracted workload duration for trainers: {}", usernames);
    }
}
