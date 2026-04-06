package com.solara.mnote.controllers;

import com.solara.mnote.models.dto.NoteCreateDTO;
import com.solara.mnote.models.dto.NoteUpdateDTO;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.repo.NoteRepository;
import com.solara.mnote.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notes")
public class NoteController{

    private final NoteService noteService;
    private final NoteRepository noteRepository;

    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody NoteCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable UUID id, @RequestBody NoteUpdateDTO dto) {
        return ResponseEntity.ok(noteService.patch(id, dto));
    }

}

