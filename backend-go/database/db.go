package database

import (
	"database/sql"
	"fmt"
	"log"
	"mnote/models"
	"strings"

	_ "modernc.org/sqlite" // Pure Go driver
)

type DBManager struct {
	pragma string
	name   string
}

func Initialize(name, pragma string) *DBManager {
	return &DBManager{
		name:   name,
		pragma: pragma,
	}
}

func (dbm *DBManager) OpenDB() {
	//open the database
	db, err := sql.Open("sqlite", dbm.pragma)
	if err != nil {
		panic(err)
	}
	defer db.Close()

	createQuery := `CREATE TABLE IF NOT EXISTS NotesTable (id INTEGER PRIMARY KEY AUTOINCREMENT, 
														   note TEXT, 
														   tag TEXT,
														   sentToSpring BOOLEAN NOT NULL DEFAULT 0,
														   created_at DATETIME DEFAULT CURRENT_TIMESTAMP)`
	if _, err := db.Exec(createQuery); err != nil {
		log.Fatal(err)
	}
}

func (dbm *DBManager) InsertNote(note models.NoteRequest) (string, int64) {
	db, err := sql.Open("sqlite", dbm.pragma)
	if err != nil {
		panic(err)
	}
	defer db.Close()

	// placeholders (?) to prevent SQL injection
	res, err := db.Exec("INSERT INTO NotesTable(note, tag, created_at) VALUES(?, ?, ?)",
		note.Content, note.Tag, note.CreatedAt)
	if err != nil {
		log.Fatal(err)
	}
	id, _ := res.LastInsertId()
	fmt.Printf("\nInserted record ID: %d\n", id)
	return "OK", id
}

func (dbm *DBManager) UpdateNote(id int64, opts models.UpdateNoteOptions) error {
	db, err := sql.Open("sqlite", dbm.pragma)
	if err != nil {
		panic(err)
	}
	defer db.Close()

	updates := []string{}
	args := []interface{}{}

	if opts.Content != nil {
		updates = append(updates, "note = ?")
		args = append(args, *opts.Content)
	}
	if opts.Tag != nil {
		updates = append(updates, "tag = ?")
		args = append(args, *opts.Tag)
	}
	if opts.SentToSpring != nil {
		updates = append(updates, "sentToSpring = ?")
		args = append(args, *opts.SentToSpring)
	}

	if len(updates) == 0 {
		return fmt.Errorf("no fields to update")
	}

	query := "UPDATE NotesTable SET " + strings.Join(updates, ", ") + " WHERE id = ?"
	args = append(args, id)

	_, err = db.Exec(query, args...)
	return err
}

/*
func (dbm *DBManager) DeleteNoteByID(id int64) error {
	// Simple, specific method
}

type GetNoteOptions struct {
	ID           *int64
	Tag          *string
	SentToSpring *bool
	Limit        *int
}

func (dbm *DBManager) GetNotes(opts GetNoteOptions) ([]models.NoteRequest, error) {
	// Build WHERE clauses dynamically
}
*/
