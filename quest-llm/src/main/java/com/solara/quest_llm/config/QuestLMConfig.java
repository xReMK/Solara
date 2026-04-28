package com.solara.quest_llm.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class QuestLMConfig {

    @Bean("lmStudio")
    @Qualifier("lmStudio")
    public RestClient lmStudioRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://localhost:1234/api/v1").build();
    }

    @Bean("lmOpenAI")
    @Qualifier("lmOpenAI")
    public RestClient lmOpenAIRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://localhost:1234/v1").build();
    }
}
