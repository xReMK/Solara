package notes

import (
	"bytes"
	"encoding/json"
	"fmt"
	"mnote/database"
	"mnote/models"
	"sync"
	"time"

	"net/http"
)

var (
	globalNoteManager *NoteManager
	noteMgrMutex      sync.Mutex
	dbm               *database.DBManager
	dbMgrMutex        sync.Mutex
)

type NoteManager struct {
	Queue       chan models.NoteRequest
	FailedQueue chan models.NoteRequest
	SpringURL   string
	TimeoutSecs time.Duration
}

func NewNoteManager(queue chan models.NoteRequest, failedQueue chan models.NoteRequest, springURL string) *NoteManager {
	if springURL == "" {
		springURL = "http://localhost:8080/api/notes"
	}

	return &NoteManager{
		Queue:       queue,
		FailedQueue: failedQueue,
		SpringURL:   springURL,
		TimeoutSecs: 10 * time.Second,
	}
}

// SetNoteManager sets the global singleton NoteManager instance (call once at startup)
func SetNoteManager(nm *NoteManager) {
	noteMgrMutex.Lock()
	defer noteMgrMutex.Unlock()
	globalNoteManager = nm
}

// GetNoteManager returns the global singleton NoteManager instance (thread-safe)
func GetNoteManager() *NoteManager {
	noteMgrMutex.Lock()
	defer noteMgrMutex.Unlock()
	return globalNoteManager
}

func (nm *NoteManager) Initialize() {
	// db initialize & open
	dbm = database.Initialize("notes.db", "file:notes.db?_pragma=journal_mode(WAL)&_pragma=synchronous=NORMAL&_pragma=busy_timeout(5000)")
	dbm.OpenDB()
}

func (nm *NoteManager) ProcessNote(content string) error {
	noteReq := models.NoteRequest{
		Content:   content,
		CreatedAt: time.Now().Format(time.RFC3339),
		Tag:       "general",
	}

	resDb, lastId := dbm.InsertNote(noteReq)
	if resDb == "OK" && lastId != 0 {
		fmt.Println("Note inserted successfully in DB")
	} else {
		//should retry or add to notes_failed.txt or notesFailed queue/channel
	}

	if err := nm.SendToSpring(noteReq); err != nil {
		nm.FailedQueue <- noteReq
		return fmt.Errorf("failed to send note to Spring: %w", err)
	} else {
		//update sentToSpring note status as true
		dbm.UpdateNote(lastId, models.UpdateNoteOptions{SentToSpring: models.Ptr(true)})
	}

	fmt.Println("Note sent to Spring service")

	return nil
}

func (nm *NoteManager) SendToSpring(noteReq models.NoteRequest) error {
	jsonData, err := json.Marshal(noteReq)
	if err != nil {
		return fmt.Errorf("failed to marshal json: %w", err)
	}

	// Use a client with timeout to prevent hanging indefinitely
	// Production tip: Never use http.DefaultClient without a timeout
	client := &http.Client{
		Timeout: nm.TimeoutSecs,
	}

	resp, err := client.Post(nm.SpringURL, "application/json", bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("POST request failed: %w", err)
	}

	// Always close the body to return the TCP connection to the pool
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		return fmt.Errorf("spring returned error: %d", resp.StatusCode)
	}

	return nil
}

func (nm *NoteManager) ProcessFailedNotes() {
	for note := range nm.FailedQueue {
		fmt.Println(note)
	}
}
