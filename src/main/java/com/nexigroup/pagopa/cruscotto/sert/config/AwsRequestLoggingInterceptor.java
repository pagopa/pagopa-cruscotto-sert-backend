package com.nexigroup.pagopa.cruscotto.sert.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpRequest;

@Component
public class AwsRequestLoggingInterceptor implements ExecutionInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(AwsRequestLoggingInterceptor.class);

    @Override
    public SdkHttpRequest modifyHttpRequest(Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
        LOG.debug("=========================== AWS REQUEST ===========================");
        LOG.debug("URI: {}", context.httpRequest().getUri());
        LOG.debug("Method: {}", context.httpRequest().method());
        LOG.debug("Headers: {}", context.httpRequest().headers());
        LOG.debug("==============================================================");
        return context.httpRequest();
    }

    @Override
    public void afterExecution(Context.AfterExecution context, ExecutionAttributes executionAttributes) {
        LOG.debug("=========================== AWS RESPONSE ===========================");
        LOG.debug("Status: {}", context.httpResponse().statusCode());
        LOG.debug("Headers: {}", context.httpResponse().headers());
        LOG.debug("===============================================================");
    }
}
