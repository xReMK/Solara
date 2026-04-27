package com.solara.quest_llm.services;

import com.solara.quest_llm.models.LmModelResponse;
import com.solara.quest_llm.models.lmstudio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;



@RequiredArgsConstructor
@Service
public class LmStudioClient {
    private final RestClient restClient;
    /**
     * Returns a list of keys for all models that have at least one
     * loaded instance (ready to chat).
     */
    public List<LmModelResponse.ModelData> getActiveModelKeys() {
        LmModelResponse response = restClient.get()
                .uri("/models")
                .retrieve()
                .body(LmModelResponse.class); // Spring auto-converts JSON to your Record

        response.models().forEach(model -> {
            System.out.println("Model: " + model.displayName());
            System.out.println("Is Loaded: " + !model.loadedInstances().isEmpty());
        });

        return response.models();
    }
    public String getRawChatResponse(String userPrompt) {
        userPrompt = "hello, how are you doing?";
        var request = new LmModelResponse.ChatRequest(
                "dolphin3-8k",
                List.of(new LmModelResponse.Message("user", userPrompt)),
                0.7
        );

        return restClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class); // You can map this to a proper ChatResponse DTO later
    }

    // real shit here
    // 1) POST : /api/v1/chat
    public LmChatResponse chatWithTools(LmChatRequest request) {
        return restClient.post()
                .uri("/chat")
                .body(request)
                .retrieve()
                .body(LmChatResponse.class);
    }

    // 2) GET : /api/v1/models
    public List<LmModelEntry> getModels() {
        LmModelListResponse response = restClient.get()
                .uri("/models")
                .retrieve()
                .body(LmModelListResponse.class);
        return response != null ? response.models() : List.of();
    }

    // 3) POST : /api/v1/models/load
    public void loadModel(LmLoadRequest request) {
        restClient.post()
                .uri("/models/load")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    // 4) POST : /api/v1/models/download
    public String downloadModel(String modelKey) {
        Map<String, String> response = restClient.post()
                .uri("/models/download")
                .body(new LmDownloadRequest(modelKey))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return response.get("job_id");
    }

    // 5) POST : /api/v1/models/unload
    public void unloadModel(String instanceId) {
        restClient.post()
                .uri("/models/unload")
                .body(new LmUnloadRequest(instanceId))
                .retrieve()
                .toBodilessEntity();
    }

    // 6) GET : /api/v1/models/download/status/:job_id
    public LmDownloadStatusResponse getDownloadStatus(String jobId) {
        return restClient.get()
                .uri("/models/download/status/{job_id}", jobId)
                .retrieve()
                .body(LmDownloadStatusResponse.class);
    }

}
