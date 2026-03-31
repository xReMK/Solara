package utils

import (
	"strings"
)

func CleanNoteTags(tags []string) []string {
	var cleaned []string

	rawJoined := strings.Join(tags, ",")
	parts := strings.Split(rawJoined, ",")

	for _, st := range parts {
		st = strings.TrimSpace(st)
		st = strings.TrimLeft(st, "#")
		if st != "" {
			cleaned = append(cleaned, st)
		}
	}
	return cleaned
}

func UniqueNoteTags(tags []string) []string {
	var uniqueTags []string
	mapTags := make(map[string]struct{})

	for _, tag := range tags {
		// Normalize : backup trimming space & # if missed earlier
		tag = strings.TrimSpace(strings.TrimLeft(tag, "#"))
		if _, exists := mapTags[tag]; !exists && tag != "" {
			mapTags[tag] = struct{}{}
			uniqueTags = append(uniqueTags, tag)
		}
	}
	return uniqueTags
}

// Note : Using struct{} in a map is a senior-level optimization because it occupies 0 bytes of memory compared to bool

func ParseNoteImportance(importance string) int {
	return strings.Count(importance, "*")
}
