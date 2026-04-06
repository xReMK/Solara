package com.solara.mnote.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.Set;

public class NoteCreateDTO {
    @JsonProperty("id")
    @NotNull(message = "UUID is required")
    private String Id;

    @JsonProperty("content")
    @NotBlank(message = "Content cannot be empty")
    @Size(max = 5000, message = "Note is too long")
    private String content;

    @JsonProperty("tags")
    private Set<String> tags;

    @JsonProperty("importance")
    @Min(0) @Max(5)
    private Integer importance;

    // Jackson handles the parsing automatically if it's standard ISO-8601/RFC3339
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

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
    public Integer getImportance(){ return importance;}
    public void setImportance(Integer importance){ this.importance = importance; }
}
