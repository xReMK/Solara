package com.solara.mnote.service;

import com.solara.mnote.models.kafkaDto.responses.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class NoteResponseConsumer {

    @KafkaListener(topics = "embeddings-response")
    public void consume(EmbeddingResponse response){
        System.out.println("Received embeddings for note : "+response.getNoteId()+" embeddings : "+ Arrays.toString(response.getVectors()));

        // call to save/update embeddings to postgres
    }
}
