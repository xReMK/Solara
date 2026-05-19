package com.solara.mnote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solara.mnote.models.kafkaDto.requests.EmbeddingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteRequestPublisher {
    // similar to RestClient for Http/Rest calls
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String TOPIC = "embeddings-request";

    public void publishEmbeddingRequest(UUID noteId, String text){
        try{
            EmbeddingRequest request = new EmbeddingRequest(noteId,text);

            // Serialize the POJO directly to a raw JSON String
            String jsonPayload = objectMapper.writeValueAsString(request);

            // Sending noteId as the 'Key' ensures all updates for the same note
            // go to the same partition (order preservation)
            kafkaTemplate.send(TOPIC,noteId.toString(),jsonPayload);
            System.out.println("Note "+noteId+" sent to Kafka");

        } catch (JsonProcessingException e) {
            // Log explicitly to avoid silent failure bubbles in async execution blocks
            System.err.println("Failed to serialize embedding request for note: " + noteId + " Error: " + e.getMessage());
        }

    }
}
