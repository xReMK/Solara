package com.solara.quest_llm.models.lmstudioopenai;

public record LmOpenAIEmbeddingRequest(
        String model,
        String input
) {}
