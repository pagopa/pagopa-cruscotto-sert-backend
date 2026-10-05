package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;

class AsyncConfigurationTest {

    @Test
    void createsExecutorAndDefaultAsyncExceptionHandler() {
        AsyncConfiguration configuration = new AsyncConfiguration(new TaskExecutionProperties());

        Executor executor = configuration.getAsyncExecutor();

        assertThat(executor).isNotNull();
        assertThat(configuration.getAsyncUncaughtExceptionHandler()).isInstanceOf(SimpleAsyncUncaughtExceptionHandler.class);
    }
}