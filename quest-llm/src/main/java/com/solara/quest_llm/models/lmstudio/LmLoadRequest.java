package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LmLoadRequest(
        String model,
        @JsonProperty("context_length") Integer contextLength,
        @JsonProperty("flash_attention") boolean flashAttention,
        @JsonProperty("echo_load_config") boolean echoLoadConfig
) {}