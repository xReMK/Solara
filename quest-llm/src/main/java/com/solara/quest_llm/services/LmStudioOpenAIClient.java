package com.solara.quest_llm.services;


import com.solara.quest_llm.models.lmstudioopenai.LmOpenAIEmbeddingRequest;
import com.solara.quest_llm.models.lmstudioopenai.LmOpenAIEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LmStudioOpenAIClient {
    @Qualifier("lmOpenAI")
    private final RestClient restClient;

    public float[] getEmbeddingsOpenAI(LmOpenAIEmbeddingRequest request){
        LmOpenAIEmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .body(request)
                .retrieve()
                .body(LmOpenAIEmbeddingResponse.class);

        List<Double> vectorEmbeddings = response.data().getFirst().embedding();
        float[] vector = new float[vectorEmbeddings.size()];
        for (int i = 0; i < vectorEmbeddings.size(); i++) {
            vector[i] = vectorEmbeddings.get(i).floatValue();
        }
        System.out.println("vectorEmbeddings : "+Arrays.toString(vector));
        return vector;
    }
}
