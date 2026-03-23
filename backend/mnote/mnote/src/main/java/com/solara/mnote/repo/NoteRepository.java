package com.solara.mnote.repo;

import com.solara.mnote.models.dto.NoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<NoteRequest, Long> {
    // Optimization: JOIN FETCH ensures tags are loaded in 1 query, not N queries.
    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags WHERE n.id = :id")
    Optional<NoteRequest> findByIdWithTags(@Param("id") Long id);

    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags ORDER BY n.createdAt DESC")
    List<NoteRequest> findAllWithTags();
}
