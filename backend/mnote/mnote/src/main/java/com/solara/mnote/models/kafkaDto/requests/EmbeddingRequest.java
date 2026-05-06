package com.solara.mnote.models.kafkaDto.requests;

import java.util.UUID;

public record EmbeddingRequest(UUID noteId, String text) {}