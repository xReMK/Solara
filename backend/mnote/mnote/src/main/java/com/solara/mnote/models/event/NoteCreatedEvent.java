package com.solara.mnote.models.event;
import com.solara.mnote.models.entity.Note;

public record NoteCreatedEvent(Note note) {}
