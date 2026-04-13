package com.solara.mnote.controllers;

import com.solara.mnote.models.dto.NoteCreateDTO;
import com.solara.mnote.models.dto.NoteUpdateDTO;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.models.responses.NoteResponse;
import com.solara.mnote.repo.NoteRepository;
import com.solara.mnote.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GetMapping
    public List<NoteResponse> fetchNotes(@RequestParam(required = false) String tag){
        List<Note> notes = (tag != null && !tag.isBlank())
                ? noteRepository.findByTagsContaining(tag)
                : noteRepository.findAllByOrderByCreatedAtDesc();

        return notes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

    }

    public NoteResponse convertToDto(Note note){
        NoteResponse noteResponse = new NoteResponse();
        noteResponse.setId(note.getId().toString());
        noteResponse.setContent(note.getContent());
        noteResponse.setImportance(note.getImportance());
        noteResponse.setTags(note.getTags());

        return noteResponse;
    }

}
/*
@GetMapping
public Page<NoteResponse> fetchNotes(
        @RequestParam(required = false) String tag,
        Pageable pageable) { // Spring populates this from ?page=x&size=y

    Page<Note> notes = (tag != null && !tag.isBlank())
            ? noteRepository.findByTagsContaining(tag, pageable)
            : noteRepository.findAll(pageable);

    return notes.map(this::convertToDto);
}
 */
