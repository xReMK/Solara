package com.solara.quest_llm.controllers;

import com.solara.quest_llm.models.LmModelResponse;
import com.solara.quest_llm.models.lmstudio.LmModelEntry;
import com.solara.quest_llm.services.LmStudioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class DolphinController {

    private final LmStudioClient lmStudioClient;

    @GetMapping("/models")
    public List<LmModelEntry> getModels(){
        return lmStudioClient.getModels();
    }

    @PostMapping("/chat")
    public String postPrompt(@RequestBody String prompt){
        return lmStudioClient.getRawChatResponse(prompt);
    }
}
