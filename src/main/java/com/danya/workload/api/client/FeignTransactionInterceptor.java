package com.danya.workload.api.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

class FeignTransactionInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String transactionId = MDC.get("transactionId");
        if (transactionId != null) {
            template.header("X-Transaction-Id", transactionId);
        }
    }
}
