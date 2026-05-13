package com.solara.quest_llm.services;

import com.solara.quest_llm.models.kafkaDto.requests.NoteEmbeddingRequest;
import com.solara.quest_llm.models.lmstudioopenai.LmOpenAIEmbeddingRequest;
import lombok.RequiredArgsConstructor;
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

    @KafkaListener(topics = "embeddings-request")
    public void consume(NoteEmbeddingRequest request){
        System.out.println("Received note "+ request.getNoteId().toString()+" from Kafka");
        float[] vectors = lmStudioOpenAIClient.getEmbeddingsOpenAI(new LmOpenAIEmbeddingRequest(model,request.getText()));

        noteResponsePublisher.publishNoteEmbeddingResponse(request.getNoteId(),vectors);
    }

}
