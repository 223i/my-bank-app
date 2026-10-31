package com.iron.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public Counter notificationFailureCounter(MeterRegistry registry) {
        return Counter.builder("notification_failures_total")
                .tag("service", "accounts-service")
                .register(registry);
    }
}
