package com.finsight.api.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dummy replacement for the original DataLoader.
 * It exists only so that Spring’s @ComponentScan can still find
 * com.finsight.api.config.DataLoader and the application starts.
 *
 * If you later want to seed data again, replace the lambda body
 * with your insert logic or delete the class and remove the
 * reference from any @Import / @ComponentScan.
 */
@Configuration
public class DataLoader {

    /**
     * A no-op ApplicationRunner – Spring runs it after the context
     * is ready, but the body is empty.
     */
    @Bean
    public ApplicationRunner emptyRunner() {
        return args -> {
            // intentionally left blank
        };
    }
}

