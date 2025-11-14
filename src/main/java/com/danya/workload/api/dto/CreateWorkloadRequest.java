package com.danya.workload.api.dto;

import lombok.Builder;

@Builder
public record CreateWorkloadRequest(
        String username,
        String firstName,
        String lastName
) {}
