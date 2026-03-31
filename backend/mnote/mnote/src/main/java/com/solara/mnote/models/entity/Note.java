package com.solara.mnote.models.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "notes")
public class Note{

    @Id
    private UUID Id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "importance")
    private int importance;

    // A single note cannot have the same tag twice
    /*
    By changing the Java type from List<String> to Set<String>, Hibernate automatically handles basic deduplication in memory
    However, to be "sophisticated," we add a Unique Constraint to the join table
     */
    @ElementCollection // This tells JPA: "Create a separate table for this List"
    @CollectionTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            // This creates a composite unique index: (note_id, tag)
            uniqueConstraints = @UniqueConstraint(columnNames = {"note_id","tag"})
    )
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    public UUID getId(){ return Id; }
    public void setId(UUID uuid){ this.Id = uuid;}
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
