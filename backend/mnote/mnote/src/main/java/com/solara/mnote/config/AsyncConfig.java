package com.solara.mnote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling //understand more about these annotations, where & how exactly does spring load this class & how it and the methods which it hosts impact the project
public class AsyncConfig {

    @Bean(name = "noteTaskExecutor")
    public Executor noteTaskExecutor(){
        int cores = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores*2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("mnote-async-");
        executor.initialize();
        return executor;
    }
}
