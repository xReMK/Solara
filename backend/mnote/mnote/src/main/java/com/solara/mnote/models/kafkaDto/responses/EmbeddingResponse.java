package com.solara.mnote.models.kafkaDto.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingResponse {
    private UUID noteId;
    private float[] vectors;
}
