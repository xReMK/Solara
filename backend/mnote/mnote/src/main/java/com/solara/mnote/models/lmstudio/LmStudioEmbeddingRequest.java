package com.solara.mnote.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LmStudioEmbeddingRequest {
    @JsonProperty("model")
    String model;
    @JsonProperty("input")
    String input;

    public LmStudioEmbeddingRequest(String model,String input){
        this.input = input;
        this.model = model;
    }
}
