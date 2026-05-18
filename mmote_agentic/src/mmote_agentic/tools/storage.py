import sqlite3
import json
from datetime import datetime


def save_notes_to_sqlite(json_string_output: str):
    """Parses the raw JSON string from the crew task and writes to an embedded SQLite DB."""
    conn = sqlite3.connect("mnote_local.db")
    cursor = conn.cursor()

    # Initialize an embedded relational schema if it doesn't exist
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS atomic_notes (
            id TEXT PRIMARY KEY,
            content TEXT,
            importance INTEGER,
            tags TEXT,
            created_at TEXT
        )
    """)

    try:
        # Clean potential markdown wrapping indicators if present
        clean_json = json_string_output.strip().lstrip("```json").rstrip("```")
        notes_data = json.loads(clean_json)

        # Support both a single object or an array of objects
        if not isinstance(notes_data, list):
            notes_data = [notes_data]

        for note in notes_data:
            cursor.execute("""
                INSERT OR REPLACE INTO atomic_notes (id, content, importance, tags, created_at)
                VALUES (?, ?, ?, ?, ?)
            """, (
                note.get("id"),
                note.get("content"),
                note.get("importance", 2),
                json.dumps(note.get("tags", [])),
                note.get("createdAt")
            ))
        conn.commit()
    except Exception as e:
        print(f"Database insertion rollback. Error parsing payload: {e}")
    finally:
        conn.close()
