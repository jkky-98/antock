package com.antock.task.service.externalapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "teleSalesTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(15);        // 최대 동시 처리 개수 cpu 코어 수 x 2.5 설정 추천
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("tele-sales-async-");
        executor.initialize();
        return executor;
    }
}
