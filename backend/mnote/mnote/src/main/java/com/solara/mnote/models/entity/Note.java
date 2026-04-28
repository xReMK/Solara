package com.solara.mnote.models.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
type Note struct {
    id        UUID    `json:"id"`
    content     string `json:"title"`
    importance   int `json:"content"`
    tags        []string
    createdAt string
}
 */

@Entity
@Table(name = "notes")
public class Note{

    @Id
    @JdbcTypeCode(SqlTypes.UUID) // Ensures Hibernate maps it to Postgres UUID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "content",columnDefinition = "TEXT")
    private String content;

    @Column(name = "importance")
    private int importance;

    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

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

    public UUID getId(){ return id; }
    public void setId(UUID uuid){ this.id = uuid;}
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
