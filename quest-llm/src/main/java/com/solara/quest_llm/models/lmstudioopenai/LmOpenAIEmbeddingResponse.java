package com.solara.quest_llm.models.lmstudioopenai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LmOpenAIEmbeddingResponse(
        @JsonProperty("object") String object,
        @JsonProperty("data") List<EmbeddingData> data,
        @JsonProperty("model") String model,
        @JsonProperty("usage") Usage usage
) {}



