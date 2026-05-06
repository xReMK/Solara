package com.solara.mnote.service;

import com.solara.mnote.models.entity.Note;
import com.solara.mnote.models.event.NoteCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class NoteServiceProcessor {

    private final RestClient restClient;
    private final NoteEventPublisher noteEventPublisher;

    @Async("noteTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processNote(NoteCreatedEvent event){
        System.out.println("INSIDE NoteServiceProcessor :: processNote");
        Note note = event.note();
        // Simulate heavy work (e.g., generating search indexes)

        /*
        LmStudioEmbeddingRequest embeddingRequest = new LmStudioEmbeddingRequest("text-embedding-nomic-embed-text-v1.5",note.getContent());

        float[] vectors = restClient.post()
                .uri("/v1/embeddings")
                .body(embeddingRequest)
                .retrieve()
                .body(float[].class);

        System.out.println("INSIDE NoteServiceProcessor :: vectors : "+ Arrays.toString(vectors));
        */

        noteEventPublisher.publishEmbeddingRequest(note.getId(),note.getContent());

        try {
            Thread.sleep(10000);
            System.out.println("Background processing complete for: " + note.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    /*

    imp note :

    - Connection Deadlock Risk: Since the transaction has already committed, the AFTER_COMMIT phase is technically "clean-up" time
    If you try to perform more database operations in a synchronous listener during this phase, you might get errors because
    the original database connection might have already been closed or returned to the pool

    - Read-Only State: By the time AFTER_COMMIT runs, the transaction is over. Without a new @Transactional or a separate thread,
    you cannot easily save new data (like the generated vectors) back to the database

    so that means once you get the float[] we shouldn't call another function from here to update the database with this value
    for this, maybe another background service can be started/or which keeps running for every 5 minutes & it processes all notes
    from the database where the <vector> column is nil.

     */
}
