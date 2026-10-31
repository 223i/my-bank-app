package com.iron.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public Counter failedCashOperationsCounter(MeterRegistry registry) {
        return Counter.builder("failed_cash_operations_total")
                .tag("service", "cash-service")
                .register(registry);
    }
}
