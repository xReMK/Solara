package com.solara.mnote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solara.mnote.models.kafkaDto.responses.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class NoteResponseConsumer {

    private final NoteService noteService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "embeddings-response", containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(String rawJsonPayload){
        try{
            EmbeddingResponse response = objectMapper.readValue(rawJsonPayload, EmbeddingResponse.class);
            System.out.println("Received embeddings for note : "+response.getNoteId()+" embeddings : "+ Arrays.toString(response.getVectors()));

            // call to save/update embeddings to postgres
            noteService.updateNoteVector(response.getNoteId(),response.getVectors());
        } catch (JsonProcessingException e) {
            // Critical Senior Pattern: Catching parsing errors here prevents poison-pill scenarios.
            // The unparseable message is dropped or logged, and the offset safely commits instead of halting the consumer.
            System.err.println("Poison pill detected! Failed to deserialize incoming payload frame: " + e.getMessage());
        }

    }
}
