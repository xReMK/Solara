package com.solara.mnote.controllers;

import com.solara.mnote.models.dto.NoteRequest;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.repo.NoteRepository;
import com.solara.mnote.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class NoteController{

    private final NoteService noteService;
    private final NoteRepository noteRepository;

    @PostMapping("/api/notes")
    public ResponseEntity<Note> insertNote(@RequestBody NoteRequest noteRequest) {
        System.out.println("Received: " + noteRequest.getContent());
        boolean exists = noteRepository.existsById(UUID.fromString(noteRequest.getId()));
        Note note = noteService.saveOrUpdate(noteRequest);

        if(exists){
            return ResponseEntity.ok(note);
        } else{
            return ResponseEntity.status(HttpStatus.CREATED).body(note);
        }
    }

}

