package com.solara.mnote.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public class NoteRequest{
    @JsonProperty("content")
    private String content;

    @JsonProperty("tags")
    private List<String> tags;

    // Jackson handles the parsing automatically if it's standard ISO-8601/RFC3339
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime timeStamp) {
        this.createdAt = timeStamp;
    }
}
