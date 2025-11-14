package com.danya.workload.api.dto;

import java.util.List;

public record BatchUpdateWorkloadRequest(
        List<UpdateWorkloadRequest> updates
) {}
