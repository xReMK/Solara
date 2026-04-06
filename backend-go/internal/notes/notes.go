package notes

import (
	"bytes"
	"encoding/json"
	"fmt"
	"mnote/database"
	"mnote/models"
	"net/http"
	"sync"
	"time"
)

var (
	globalNoteManager *NoteManager
	noteMgrMutex      sync.Mutex
	dbm               *database.DBManager
	dbMgrMutex        sync.Mutex
)

type NoteManager struct {
	Queue       chan models.NoteAddRequest
	FailedQueue chan models.NoteAddRequest
	SpringURL   string
	TimeoutSecs time.Duration
}

func NewNoteManager(queue chan models.NoteAddRequest, failedQueue chan models.NoteAddRequest, springURL string) *NoteManager {
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

func (nm *NoteManager) ProcessNote(env models.Envelope) (string, error) {
	var msg string
	var err error

	switch env.Action {
	case models.ActionAdd:
		var req models.NoteAddRequest
		json.Unmarshal(env.Payload, &req)
		msg, err = nm.SendToSpring("POST", "/api/notes", req)

	case models.ActionUpdate:
		var req models.NoteUpdateRequest
		json.Unmarshal(env.Payload, &req)
		// Call PATCH /api/notes/{id}
		msg, err = nm.SendToSpring("PATCH", "/api/notes/"+req.ID, req)

	default:
		return "", fmt.Errorf("unknown action: %s", env.Action)
	}
	//fmt.Println("Note sent to Spring service")

	return msg, err
}

func (nm *NoteManager) SendToSpring(method string, path string, body any) (string, error) {
	jsonData, err := json.Marshal(body)
	if err != nil {
		return "", fmt.Errorf("marshal error: %w", err)
	}
	url := "http://localhost:8080" + path
	req, _ := http.NewRequest(method, url, bytes.NewBuffer(jsonData))
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return "", fmt.Errorf("spring service unreachable: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 200 && resp.StatusCode < 300 {
		return "Success", nil
	}

	return "", fmt.Errorf("spring returned error: %d", resp.StatusCode)
}

func (nm *NoteManager) ProcessFailedNotes() {
	for note := range nm.FailedQueue {
		fmt.Println(note)
	}
}
