package com.solara.quest_llm.models.kafkaDto.requests;

public record EmbeddingRequest(Long noteId, String text) {}