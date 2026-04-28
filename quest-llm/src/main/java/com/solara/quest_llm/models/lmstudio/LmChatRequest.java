package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LmChatRequest(
        String model,
        String input,
        List<LmIntegration> integrations,
        @JsonProperty("context_length") Integer contextLength,
        Double temperature
) {}