package com.solara.mnote.models.responses;

import java.util.Set;

public class NoteResponse {
    private String id;
    private String content;
    private Set<String> tags;
    private Integer importance;

    public String getId(){ return id;}
    public void setId(String Id) { this.id = Id; }
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
    public Integer getImportance(){ return importance;}
    public void setImportance(Integer importance){ this.importance = importance; }
}
