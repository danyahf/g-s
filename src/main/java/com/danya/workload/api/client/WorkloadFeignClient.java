package com.danya.workload.api.client;

import com.danya.workload.api.dto.BatchUpdateWorkloadRequest;
import com.danya.workload.api.dto.CreateWorkloadRequest;
import com.danya.workload.api.dto.UpdateWorkloadRequest;
import com.danya.workload.api.dto.WorkloadDurationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.YearMonth;

@FeignClient(name = "workload-service", configuration = FeignConfig.class)
public interface WorkloadFeignClient {

    @PostMapping("/workloads")
    void createWorkload(CreateWorkloadRequest payload);

    @PutMapping("/workloads/duration/add")
    void addDuration(UpdateWorkloadRequest payload);

    @PutMapping("/workloads/duration/subtract/batch")
    void subtractDuration(BatchUpdateWorkloadRequest payload);

    @GetMapping("/workloads/{username}/totalDuration/{yearMonth}")
    WorkloadDurationResponse getTotalDuration(
            @PathVariable String username,
            @PathVariable YearMonth yearMonth
    );
}
