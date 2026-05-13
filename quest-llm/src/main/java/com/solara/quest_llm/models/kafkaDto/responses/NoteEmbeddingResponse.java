package com.solara.quest_llm.models.kafkaDto.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteEmbeddingResponse {
    private UUID noteId;
    private float[] vectors;
}
