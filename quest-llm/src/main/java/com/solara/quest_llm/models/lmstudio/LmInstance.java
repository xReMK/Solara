package com.solara.quest_llm.models.lmstudio;

import java.util.Map;

public record LmInstance(String id, Map<String, Object> config) {}