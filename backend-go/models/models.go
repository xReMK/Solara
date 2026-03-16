package models

// NoteRequest represents a note being sent to the backend
type NoteRequest struct {
	Content   string `json:"content"`
	Timestamp string `json:"timestamp"`
	Tag       string `json:"tag"`
}
