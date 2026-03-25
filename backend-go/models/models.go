package models

// NoteRequest represents a note being sent to the backend
type NoteRequest struct {
	Content    string   `json:"content"`
	CreatedAt  string   `json:"createdAt"`
	Tags       []string `json:"tags"`
	Importance int      `json:"importance"`
}

// UpdateNoteOptions for flexible updates (only provided fields are updated)
type UpdateNoteOptions struct {
	Content      *string
	Tag          *string
	SentToSpring *bool
}

// Ptr returns a pointer to the given value (helper for creating optional fields)
func Ptr[T any](v T) *T {
	return &v
}
