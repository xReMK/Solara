package utils

import (
	"strings"
)

func CleanNoteTags(tags []string) []string {
	var cleaned []string
	for _, tag := range tags {
		cleaned = append(cleaned, strings.TrimPrefix(tag, "#"))
	}
	return cleaned
}

func ParseNoteImportance(importance string) int {
	return strings.Count(importance, "*")
}
