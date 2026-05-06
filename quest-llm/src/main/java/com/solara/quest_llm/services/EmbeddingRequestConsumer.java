package com.solara.quest_llm.services;

import com.solara.quest_llm.models.kafkaDto.requests.EmbeddingRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingRequestConsumer {

    @KafkaListener(topics = "embeddings-request", groupId = "quest-embedding-group")
    public void consume(EmbeddingRequest request){
        System.out.println("Received note "+request.noteId()+" from Kafka");

        // Logic to call LMStudio would go here
        // float[] vector = lmStudioService.getEmbedding(request.text());
    }

}
