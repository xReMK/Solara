package com.solara.mnote.Controllers;

import com.solara.mnote.Models.NoteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class NoteController{

    @PostMapping("/api/notes")
    public ResponseEntity<String> insertNote(@RequestBody NoteRequest noteRequest) {
        System.out.println("Received: " + noteRequest.getContent());
        return ResponseEntity.ok("Note processed");
    }

}

