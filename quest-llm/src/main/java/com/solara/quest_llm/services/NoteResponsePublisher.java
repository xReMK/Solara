package com.solara.quest_llm.services;

import com.solara.quest_llm.models.kafkaDto.responses.NoteEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteResponsePublisher {
    private final KafkaTemplate<String, NoteEmbeddingResponse> kafkaTemplate;
    private final String TOPIC = "embeddings-response";

    public void publishNoteEmbeddingResponse(UUID noteId, float[] vectors){
        NoteEmbeddingResponse response = new NoteEmbeddingResponse(noteId,vectors);
        kafkaTemplate.send(TOPIC,noteId.toString(),response);

        System.out.println("Note embeddings sent to Kafka : "+noteId.toString());
    }
}
