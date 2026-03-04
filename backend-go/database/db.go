package database

type DBManager struct {
	pragma string
	name   string
}

func Initialize(name, pragma string) *DBManager {
	return &DBManager{
		pragma: pragma,
		name:   name,
	}
}
