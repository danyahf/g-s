package com.danya.workload.api.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UpdateWorkloadRequest(
        String username,
        LocalDate trainingDate,
        int duration
) {}
