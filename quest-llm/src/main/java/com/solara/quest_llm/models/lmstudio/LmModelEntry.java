package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LmModelEntry(
        String type,
        String publisher,
        String key,
        @JsonProperty("display_name") String displayName,
        String architecture,
        Long sizeBytes,
        @JsonProperty("loaded_instances") List<LmInstance> loadedInstances,
        @JsonProperty("max_context_length") Integer maxContextLength,
        String format
) {}
