package com.danya.workload.api.client;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new WorkloadErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestAuthInterceptor() {
        return new FeignAuthInterceptor();
    }

    @Bean
    public RequestInterceptor requestTransactionInterceptor() {
        return new FeignTransactionInterceptor();
    }
}
