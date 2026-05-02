package com.iron.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public Counter failedCashOperationsCounter(MeterRegistry registry) {
        return Counter.builder("failed_transfers_total")
                .tag("service", "transfer-service")
                .register(registry);
    }
}
