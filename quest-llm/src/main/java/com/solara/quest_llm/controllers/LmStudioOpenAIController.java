package com.solara.quest_llm.controllers;


import com.solara.quest_llm.models.lmstudioopenai.LmOpenAIEmbeddingRequest;
import com.solara.quest_llm.services.LmStudioOpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class LmStudioOpenAIController {

    private final LmStudioOpenAIClient lmStudioOpenAIClient;

    @PostMapping("/embeddings")
    public float[] getEmbedding(@RequestBody LmOpenAIEmbeddingRequest request){
        return lmStudioOpenAIClient.getEmbeddingsOpenAI(request);
    }

}
