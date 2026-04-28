package com.solara.quest_llm.models.lmstudioopenai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Usage(
        @JsonProperty("prompt_tokens") Integer promptTokens,
        @JsonProperty("total_tokens") Integer totalTokens
) {}
