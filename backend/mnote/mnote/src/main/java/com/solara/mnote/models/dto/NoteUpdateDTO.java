package com.solara.mnote.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Set;

public class NoteUpdateDTO {
    @JsonProperty("content")
    private String content;
    @JsonProperty("add_tags")
    private Set<String> add_tags;
    @JsonProperty("remove_tags")
    private Set<String> remove_tags;
    @JsonProperty("importance")
    private Integer importance;
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    /*
    Since the JSON payload from go-service will now have "missing" fields, Spring DTO must use Wrapper classes, not primitives.
        Bad: private int importance; (Default is 0, so it always looks like the user sent a 0).
        Good: private Integer importance; (Default is null. If it's null in the JSON, Jackson leaves it null in Java)
     */

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Set<String> getAddTags() {
        return add_tags;
    }
    public void setAddTags(Set<String> add_tags) {
        this.add_tags = add_tags;
    }
    public Set<String> getRemoveTags() {
        return remove_tags;
    }
    public void setRemoveTags(Set<String> remove_tags) {
        this.remove_tags = remove_tags;
    }
    public Integer getImportance() {
        return importance;
    }
    public void setImportance(Integer importance) {
        this.importance = importance;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
