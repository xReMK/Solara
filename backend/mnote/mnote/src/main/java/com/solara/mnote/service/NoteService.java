package com.solara.mnote.service;

import com.solara.mnote.models.dto.NoteCreateDTO;
import com.solara.mnote.models.dto.NoteUpdateDTO;
import com.solara.mnote.models.entity.Note;
import com.solara.mnote.models.event.NoteCreatedEvent;
import com.solara.mnote.repo.NoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {

    //@Autowired not required because @RequiredArgsConstructor creates default & the necessary constructors
    //understand why this is better than field injection
    private final NoteRepository noteRepository;
    private final ApplicationEventPublisher eventPublisher;


    public Note mapToEntity(NoteCreateDTO dto){
        UUID uuid = UUID.fromString(dto.getId());
        Note note = new Note();
        note.setId(uuid);
        note.setContent(dto.getContent());
        note.setCreatedAt(dto.getCreatedAt());
        note.setImportance(dto.getImportance() != null ? dto.getImportance() : 0);
        if (dto.getTags() != null) {
            note.setTags(new HashSet<>(dto.getTags()));
        }
        return note;
    }

    @Transactional
    public Note create(NoteCreateDTO dto) {
        Note note = mapToEntity(dto);
        Note result = noteRepository.save(note);

        // 1. Publish the event.
        // At this point, the DB transaction is still OPEN.
        eventPublisher.publishEvent(new NoteCreatedEvent(result));

        // 2. Return to the controller (and Go service).
        // The transaction commits immediately after this return.
        return result;
    }

    @Transactional
    public Note patch(UUID id, NoteUpdateDTO dto) {
        Note existing = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        // Partial Update Logic
        if (dto.getContent() != null) {
            existing.setContent(dto.getContent());
        }
        if (dto.getImportance() != null) {
            existing.setImportance(dto.getImportance());
        }
        if (dto.getAddTags() != null) {
            existing.getTags().addAll(dto.getAddTags());
        }
        if (dto.getRemoveTags() != null) {
            existing.getTags().removeAll(dto.getRemoveTags());
        }
        // No repository.save() needed due to @Transactional Dirty Checking
        return existing;
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
