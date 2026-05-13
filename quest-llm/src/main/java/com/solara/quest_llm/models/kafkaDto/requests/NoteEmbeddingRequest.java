package com.solara.quest_llm.models.kafkaDto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteEmbeddingRequest {
    private UUID noteId;
    private String text;
}