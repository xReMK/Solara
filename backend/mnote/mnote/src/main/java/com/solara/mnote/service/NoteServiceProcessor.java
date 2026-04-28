package com.solara.mnote.service;

import com.solara.mnote.models.entity.Note;
import com.solara.mnote.models.event.NoteCreatedEvent;
import com.solara.mnote.models.lmstudio.LmStudioEmbeddingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import java.util.Arrays;

@RequiredArgsConstructor
@Component
public class NoteServiceProcessor {

    private final RestClient restClient;

    @Async("noteTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processNote(NoteCreatedEvent event){
        System.out.println("INSIDE NoteServiceProcessor :: processNote");
        Note note = event.note();
        // Simulate heavy work (e.g., generating search indexes)

        LmStudioEmbeddingRequest embeddingRequest = new LmStudioEmbeddingRequest("text-embedding-nomic-embed-text-v1.5",note.getContent());

        float[] vectors = restClient.post()
                .uri("/v1/embeddings")
                .body(embeddingRequest)
                .retrieve()
                .body(float[].class);

        System.out.println("INSIDE NoteServiceProcessor :: vectors : "+ Arrays.toString(vectors));

        try {
            Thread.sleep(10000);
            System.out.println("Background processing complete for: " + note.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
