package com.solara.mnote.service;

import com.solara.mnote.models.entity.Note;
import com.solara.mnote.models.event.NoteCreatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NoteServiceProcessor {

    @Async("noteTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processNote(NoteCreatedEvent event){
        Note note = event.note();
        // Simulate heavy work (e.g., generating search indexes)
        try {
            Thread.sleep(10000);
            System.out.println("Background processing complete for: " + note.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
