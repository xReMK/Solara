package notes

import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"sync"
	"time"

	"net/http"
)

// ============================================================================
// GLOBAL SINGLETON INSTANCE
// ============================================================================

var (
	globalNoteManager *NoteManager
	noteMgrMutex      sync.Mutex
)

// ============================================================================
// DATA TYPES
// ============================================================================

// NoteRequest defines the JSON structure for notes sent to Spring Boot API
type NoteRequest struct {
	Content   string `json:"content"`
	Timestamp string `json:"timestamp"`
}

// NoteManager manages all note operations: file storage, queue, and Spring integration
type NoteManager struct {
	Queue       chan string
	FilePath    string
	SpringURL   string
	TimeoutSecs time.Duration
}

// ============================================================================
// INITIALIZATION
// ============================================================================

// NewNoteManager creates a new NoteManager with default or provided configuration
func NewNoteManager(queue chan string, filePath string, springURL string) *NoteManager {
	if filePath == "" {
		filePath = "notes.txt"
	}
	if springURL == "" {
		springURL = "http://localhost:8080/api/notes"
	}

	return &NoteManager{
		Queue:       queue,
		FilePath:    filePath,
		SpringURL:   springURL,
		TimeoutSecs: 10 * time.Second,
	}
}

// ============================================================================
// GLOBAL SINGLETON GETTER & SETTER
// ============================================================================

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

// ============================================================================
// MAIN PROCESSING WORKFLOW
// ============================================================================

// ProcessNote coordinates the complete workflow: file storage, queue addition, Spring request, and acknowledgement
// Returns error if the entire workflow fails, nil on success
func (nm *NoteManager) ProcessNote(content string) error {
	// 1. Add to text file
	if err := nm.AddToFile(content); err != nil {
		return fmt.Errorf("failed to add note to file: %w", err)
	}

	// 2. Add to queue for background processing
	nm.AddToQueue(content)

	// 3. Send to Spring Boot API
	if err := nm.SendToSpring(content); err != nil {
		return fmt.Errorf("failed to send note to Spring: %w", err)
	}

	// 4. Wait for acknowledgement
	ack := nm.WaitForAck()
	if !ack {
		return fmt.Errorf("failed to receive acknowledgement from Spring")
	}

	return nil
}

// ============================================================================
// FILE OPERATIONS
// ============================================================================

// AddToFile appends a note to the notes text file with timestamp
func (nm *NoteManager) AddToFile(content string) error {
	// Ensure directory exists
	dir := filepath.Dir(nm.FilePath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create directory: %w", err)
	}

	// Create or append to file
	timestamp := time.Now().Format("2006-01-02 15:04:05")
	entry := fmt.Sprintf("[%s] %s\n", timestamp, content)

	f, err := os.OpenFile(nm.FilePath, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0644)
	if err != nil {
		return fmt.Errorf("failed to open notes file: %w", err)
	}
	defer f.Close()

	if _, err := f.WriteString(entry); err != nil {
		return fmt.Errorf("failed to write to notes file: %w", err)
	}

	return nil
}

// ============================================================================
// QUEUE OPERATIONS
// ============================================================================

// AddToQueue sends a note to the processing queue for background workers
func (nm *NoteManager) AddToQueue(content string) {
	nm.Queue <- content
}

// ============================================================================
// SPRING BOOT INTEGRATION
// ============================================================================

// SendToSpring sends a note to the Spring Boot API with proper timeout handling
func (nm *NoteManager) SendToSpring(content string) error {
	data := NoteRequest{
		Content:   content,
		Timestamp: time.Now().Format(time.RFC3339),
	}

	jsonData, err := json.Marshal(data)
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

// ============================================================================
// ACKNOWLEDGEMENT HANDLING
// ============================================================================

// WaitForAck waits for a brief acknowledgement that the Spring request was successful
// In this simplified implementation, a successful HTTP response counts as acknowledgement
// Can be extended to handle more complex acknowledgement protocols
func (nm *NoteManager) WaitForAck() bool {
	// For now, successful SendToSpring means acknowledgement
	// In a real scenario, this might wait for a specific callback or webhook response
	return true
}
