package com.solara.mnote.service;

import com.solara.mnote.models.dto.NoteRequest;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.repo.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public Note createNote(NoteRequest noteRequest){
        Note note = new Note();
        note.setContent(noteRequest.getContent());
        note.setTag(noteRequest.getTag());
        note.setCreatedAt(noteRequest.getCreatedAt());

        return noteRepository.save(note);
    }
}
