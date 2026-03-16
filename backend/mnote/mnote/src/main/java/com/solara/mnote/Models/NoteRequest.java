package com.solara.mnote.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoteRequest {
    @JsonProperty("content")
    private String content;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("timestamp")
    private String timeStamp;

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
