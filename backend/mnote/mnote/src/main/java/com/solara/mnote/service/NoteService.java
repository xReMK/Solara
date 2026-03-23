package com.solara.mnote.service;

import com.solara.mnote.models.dto.NoteRequest;
import com.solara.mnote.repo.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteRequest createNote(NoteRequest noteRequest){

    }
}
