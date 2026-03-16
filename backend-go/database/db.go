package database

import (
	"database/sql"
	"fmt"
	"log"
	"mnote/models"

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
														   created_at DATETIME DEFAULT CURRENT_TIMESTAMP)`
	if _, err := db.Exec(createQuery); err != nil {
		log.Fatal(err)
	}
}

func (dbm *DBManager) InsertNote(note models.NoteRequest) string {
	db, err := sql.Open("sqlite", dbm.pragma)
	if err != nil {
		panic(err)
	}
	defer db.Close()

	// placeholders (?) to prevent SQL injection
	res, err := db.Exec("INSERT INTO NotesTable(note, tag, created_at) VALUES(?, ?, ?)",
		note.Content, note.Tag, note.Timestamp)
	if err != nil {
		log.Fatal(err)
	}
	id, _ := res.LastInsertId()
	fmt.Printf("Inserted record ID: %d\n", id)
	return "OK"
}
