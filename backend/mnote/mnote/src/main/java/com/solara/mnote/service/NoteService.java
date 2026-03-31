package com.solara.mnote.service;

import com.solara.mnote.models.dto.NoteRequest;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.repo.NoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {

    //@Autowired not required because @RequiredArgsConstructor creates default & the necessary constructors
    //understand why this is better than field injection
    private final NoteRepository noteRepository;

    @Transactional
    public Note saveOrUpdate(NoteRequest noteRequest){
        //If updating an existing note and adding tags, we don't want to overwrite the old ones; we want to merge them
        UUID uuid = UUID.fromString(noteRequest.getId());
        Optional<Note> existing = noteRepository.findById(uuid);

        if(existing.isPresent()){
            Note note = existing.get();
            note.setContent(noteRequest.getContent());
            note.setCreatedAt(noteRequest.getCreatedAt());
            note.setImportance(noteRequest.getImportance());
            note.getTags().addAll(noteRequest.getTags());
            // Note: No repo.save() needed here! Hibernate syncs at method end.
            return note;
        } else{
            Note newNote = new Note();
            newNote.setId(uuid);
            newNote.setContent(noteRequest.getContent());
            newNote.setImportance(noteRequest.getImportance());
            newNote.setTags(new HashSet<>(noteRequest.getTags()));
            return noteRepository.save(newNote); // Required for new objects
        }
        /*
        Instead of
            Set<String> lol = new HashSet<>();
            lol = note.getTags();
            lol.addAll(noteRequest.getTags());
            note.setTags(lol);

        this works
            note.getTags().addAll(noteRequest.getTags());

        because In JPA, setTags(newSet) should be avoided
        Hibernate tracks the specific Collection instance it injected into your entity
        If you replace that instance with a brand new HashSet, Hibernate loses track of the "Orphan" records
        and might try to delete the entire table and re-insert everything, which is inefficient

            // 1. Get the reference to Hibernate's managed collection
            Set<String> existingTags = note.getTags();

            // 2. Clear and Add (or just AddAll)
            // If you want to REPLACE tags:
            existingTags.clear();
            existingTags.addAll(dto.getTags());

            // If you want to APPEND tags (merging):
            existingTags.addAll(dto.getTags());

            // You do NOT need note.setTags(existingTags) because existingTags
            // IS the reference to the collection inside the note object

        **ALSO:**
            existingNote.setContent(dto.getContent());  // SAFE
                Why safe? JPA tracks scalar fields (String, int, etc.) by reference only
                    setContent(newValue) → JPA sees field changed → dirty flag → UPDATE SQL
                    No "orphan" concept for primitives
                Whereas Managed Collection (Set<String> mapped to JOIN table) ❌ setTags()
                    // DANGER - Creates orphans!
                    existingNote.setTags(new HashSet<>(dto.getTags()));
                    Why dangerous? JPA tracks collection contents individually in the persistence context

        Optional<Note>:
            repository.findById(noteId) returns an Optional<Note>. This is a container that is either Full (the note exists) or Empty (it doesn't).
                If Full: The code inside .map(...) executes. It takes the "existing note" out of the box, applies your changes (like setting new content), and puts the result back into a new box.
                If Empty: The .map(...) block is skipped entirely.

            .orElseGet(...): This only runs if the box is Empty. It provides a fallback (creating a new Note)
         */

    }
}
