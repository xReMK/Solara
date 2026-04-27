package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LmIntegration(
        String type, // "ephemeral_mcp" or "plugin"
        @JsonProperty("server_label") String serverLabel,
        @JsonProperty("server_url") String serverUrl,
        @JsonProperty("id") String id, // for plugins
        @JsonProperty("allowed_tools") List<String> allowedTools
) {}
