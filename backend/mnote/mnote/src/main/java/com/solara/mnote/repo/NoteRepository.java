package com.solara.mnote.repo;

import com.solara.mnote.models.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    // Optimization: JOIN FETCH ensures tags are loaded in 1 query, not N queries.
    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags WHERE n.id = :id")
    Optional<Note> findByIdWithTags(@Param("id") UUID id);

    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags ORDER BY n.createdAt DESC")
    List<Note> findAllWithTags();

    List<Note> findAllByOrderByCreatedAtDesc();

    List<Note> findByTagsContaining(String tag);
}

/*
public interface NoteRepository extends JpaRepository<Note, UUID> {
    // Spring handles the LIMIT and OFFSET behind the scenes
    Page<Note> findAll(Pageable pageable);

    Page<Note> findByTagsContaining(String tag, Pageable pageable);
}
 */