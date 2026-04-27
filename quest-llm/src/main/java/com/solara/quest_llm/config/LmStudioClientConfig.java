package com.solara.quest_llm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class LmStudioClientConfig {
    @Bean
    public RestClient lmStudioRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://localhost:1234/api/v1").build();
    }
}
