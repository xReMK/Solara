package com.solara.mnote.service;

import com.solara.mnote.repo.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteMaintenanceService {

    private final NoteRepository repository;

    // Runs at 2:00 AM every day
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldNotes() {
        // Logic to move notes to an archive table or clean up temp data
        System.out.println("Running daily maintenance...");
    }

}
