package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.SdkHttpResponse;

class AwsRequestLoggingInterceptorTest {

    private final AwsRequestLoggingInterceptor interceptor = new AwsRequestLoggingInterceptor();

    @Test
    void returnsRequestAfterLoggingIt() {
        SdkHttpRequest request = mock(SdkHttpRequest.class);
        Context.ModifyHttpRequest context = mock(Context.ModifyHttpRequest.class);
        when(context.httpRequest()).thenReturn(request);

        assertThat(interceptor.modifyHttpRequest(context, mock(ExecutionAttributes.class))).isSameAs(request);
    }

    @Test
    void logsResponseWithoutChangingExecution() {
        Context.AfterExecution context = mock(Context.AfterExecution.class);
        when(context.httpResponse()).thenReturn(mock(SdkHttpResponse.class));

        interceptor.afterExecution(context, mock(ExecutionAttributes.class));
    }
}