package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LmStats(
        @JsonProperty("input_tokens") Integer inputTokens,
        @JsonProperty("total_output_tokens") Integer totalOutputTokens,
        @JsonProperty("tokens_per_second") Double tps
) {}