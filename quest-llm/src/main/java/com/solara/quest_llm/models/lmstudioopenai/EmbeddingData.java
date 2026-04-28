package com.solara.quest_llm.models.lmstudioopenai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EmbeddingData(
        @JsonProperty("object") String object,
        @JsonProperty("embedding") List<Double> embedding,  // Or double[] if preferred
        @JsonProperty("index") Integer index
) {}