package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LmChatResponse(
        @JsonProperty("model_instance_id") String modelInstanceId,
        List<LmOutputItem> output,
        LmStats stats,
        @JsonProperty("response_id") String responseId
) {}