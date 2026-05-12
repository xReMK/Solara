package com.solara.mnote.service;

import com.solara.mnote.models.kafkaDto.requests.EmbeddingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteEventPublisher {
    // similar to RestClient for Http/Rest calls
    private final KafkaTemplate<String, EmbeddingRequest> kafkaTemplate;
    private final String TOPIC = "embeddings-request";

    public void publishEmbeddingRequest(UUID noteId, String text){
        EmbeddingRequest request = new EmbeddingRequest(noteId,text);

        // Sending noteId as the 'Key' ensures all updates for the same note
        // go to the same partition (order preservation)
        kafkaTemplate.send(TOPIC,noteId.toString(),request);
        System.out.println("Note "+noteId+" sent to Kafka");
    }
}
