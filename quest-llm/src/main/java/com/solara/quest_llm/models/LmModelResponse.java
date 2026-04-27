package com.solara.quest_llm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record LmModelResponse(List<ModelData> models) {

    public record ChatRequest(String model, List<Message> messages, double temperature) {}
    public record Message(String role, String content) {}

    // Nesting them makes them "Static Inner Records" automatically
    public record ModelData(
            String type,
            String key,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("loaded_instances") List<Instance> loadedInstances
    ) {}

    public record Instance(
            String id,
            Map<String, Object> config
    ) {}
}