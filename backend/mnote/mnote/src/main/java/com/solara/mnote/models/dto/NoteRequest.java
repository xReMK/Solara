package com.solara.mnote.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Set;

public class NoteRequest{

    @JsonProperty("id")
    private String Id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("tags")
    private Set<String> tags;

    // Jackson handles the parsing automatically if it's standard ISO-8601/RFC3339
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("importance")
    private int importance;

    public String getId(){ return Id;}
    public void setId(String Id) { this.Id = Id; }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Set<String> getTags() {
        return tags;
    }
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime timeStamp) {
        this.createdAt = timeStamp;
    }
    public int getImportance(){ return importance;}
    public void setImportance(int importance){ this.importance = importance; }
}
