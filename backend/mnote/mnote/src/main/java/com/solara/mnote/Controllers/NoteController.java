package com.solara.mnote.controllers;

import com.solara.mnote.models.dto.NoteRequest;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class NoteController{

    private final NoteService noteService;

    @PostMapping("/api/notes")
    public ResponseEntity<String> insertNote(@RequestBody NoteRequest noteRequest) {
        System.out.println("Received: " + noteRequest.getContent());
        Note note = noteService.createNote(noteRequest);
        return note != null ? ResponseEntity.ok("note created!") : ResponseEntity.ok("failed to create note");
    }

}

