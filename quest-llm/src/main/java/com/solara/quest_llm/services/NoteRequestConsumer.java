package com.solara.quest_llm.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solara.quest_llm.models.kafkaDto.requests.NoteEmbeddingRequest;
import com.solara.quest_llm.models.kafkaDto.responses.NoteEmbeddingResponse;
import com.solara.quest_llm.models.lmstudioopenai.LmOpenAIEmbeddingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteRequestConsumer {

    @Value("${lmstudio.embedding.model}")
    private String model;
    private final LmStudioOpenAIClient lmStudioOpenAIClient;
    private final NoteResponsePublisher noteResponsePublisher;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "embeddings-request", containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(String rawJsonPayload){
        try{
            // Explicitly map incoming JSON to your domain payload model inside the consumer frame
            NoteEmbeddingRequest request = objectMapper.readValue(rawJsonPayload, NoteEmbeddingRequest.class);
            System.out.println("Received note "+ request.getNoteId().toString()+" from Kafka");
            float[] vectors = lmStudioOpenAIClient.getEmbeddingsOpenAI(new LmOpenAIEmbeddingRequest(model,request.getText()));

            noteResponsePublisher.publishNoteEmbeddingResponse(request.getNoteId(),vectors);
        } catch (JsonProcessingException e) {
            // Critical Senior Pattern: Catching parsing errors here prevents poison-pill scenarios.
            // The unparseable message is dropped or logged, and the offset safely commits instead of halting the consumer.
            System.err.println("Poison pill detected! Failed to deserialize incoming payload frame: " + e.getMessage());
        }
    }

}
