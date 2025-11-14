package com.danya.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TraineeMetricsRecorder {

    private final MeterRegistry registry;

    public TraineeMetricsRecorder(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incrementRegistration() {
        Counter.builder("trainees.new.registrations")
                .description("Number of new trainees registered")
                .register(registry)
                .increment();
    }

    public void incrementDeletion() {
        Counter.builder("trainees.removed.accounts")
                .description("Number of trainee accounts removed")
                .register(registry)
                .increment();
    }
}
