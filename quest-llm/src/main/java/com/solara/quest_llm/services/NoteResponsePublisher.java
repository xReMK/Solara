package com.solara.quest_llm.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solara.quest_llm.models.kafkaDto.responses.NoteEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteResponsePublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String TOPIC = "embeddings-response";

    public void publishNoteEmbeddingResponse(UUID noteId, float[] vectors){
        try{
            NoteEmbeddingResponse response = new NoteEmbeddingResponse(noteId,vectors);
            String jsonPayLoad =objectMapper.writeValueAsString(response);

            kafkaTemplate.send(TOPIC,noteId.toString(),jsonPayLoad);

            System.out.println("Note embeddings sent to Kafka : "+noteId.toString());
        } catch (JsonProcessingException e) {
            // Log explicitly to avoid silent failure bubbles in async execution blocks
            System.err.println("Failed to serialize embedding request for note: " + noteId + " Error: " + e.getMessage());
        }

    }
}
