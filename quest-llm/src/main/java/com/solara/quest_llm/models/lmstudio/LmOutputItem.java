package com.solara.quest_llm.models.lmstudio;

import java.util.Map;

public record LmOutputItem(
        String type, // "message" or "tool_call"
        String content, // used if type is "message"
        String tool, // used if type is "tool_call"
        Map<String, Object> arguments,
        String output // tool output
) {}
