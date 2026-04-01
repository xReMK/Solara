package models

import "encoding/json"

type CommandType string

// NoteRequest represents a note being sent to the backend
type NoteAddRequest struct {
	Id         string   `json:id`
	Content    string   `json:"content"`
	CreatedAt  string   `json:"createdAt"`
	Tags       []string `json:"tags"`
	Importance int      `json:"importance"`
}

type NoteUpdateRequest struct {
	ID string `json:id`
	// Pointers allow us to distinguish between "" and "not present"
	Content    *string   `json:"content,omitempty"`
	AddTags    *[]string `json:"add_tags,omitempty"`
	RemoveTags *[]string `json:"remove_tags,omitempty"`
	Importance *int      `json:"importance,omitempty"`
}

//The json:"...,omitempty" tag tells the Go encoder:
//"If this field is the zero value for its type (which for a pointer is nil), do not include this key in the JSON string at al

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

const (
	ActionAdd    CommandType = "ADD"
	ActionUpdate CommandType = "UPDATE"
	ActionDelete CommandType = "DELETE"
	ActionList   CommandType = "LIST"
)

type Envelope struct {
	Action  CommandType     `json:"action"`
	Payload json.RawMessage `json:"payload"` // Delayed unmarshaling
}
