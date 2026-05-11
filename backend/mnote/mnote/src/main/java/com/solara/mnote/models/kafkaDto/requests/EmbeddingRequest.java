package com.solara.mnote.models.kafkaDto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingRequest {
    private UUID noteId;
    private String text;
}