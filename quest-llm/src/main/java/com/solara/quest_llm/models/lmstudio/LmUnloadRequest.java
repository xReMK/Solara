package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LmUnloadRequest(@JsonProperty("instance_id") String instanceId) {}
