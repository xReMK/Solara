/*
Copyright © 2026 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"bufio"
	"encoding/json"
	"fmt"
	"log"
	"mnote/internal/notes"
	"mnote/models"
	"mnote/utils"
	"net"
	"os"
	"strings"
	"time"

	"github.com/Microsoft/go-winio"
	"github.com/google/uuid"
	"github.com/kardianos/service"
	"github.com/spf13/cobra"
)

// ============================================================================
// SECTION 1: DATA TYPES AND SERVICE INITIALIZATION
// ============================================================================

var (
	logger     service.Logger
	tagList    []string
	importance string
)

type program struct {
	queue       chan models.NoteRequest
	failedQueue chan models.NoteRequest
}

func getService() (service.Service, error) {
	svcConfig := &service.Config{
		Name:        "mnote",
		DisplayName: "mNote Daemon",
		Description: "Background note-taking service for Go/Spring project.",
		Arguments:   []string{"server"},
		Executable:  `D:\Solara\backend-go\mnote.exe`, //exePath, _ := os.Executable()
	}

	prg := &program{}
	return service.New(prg, svcConfig)
}

// ============================================================================
// SECTION 2: WINDOWS SERVICE LIFECYCLE MANAGEMENT
// ============================================================================

func (p *program) Start(s service.Service) error {
	// Must be non-blocking; run the Pipe Listener in a goroutine
	fmt.Printf("Service started from *p program")
	go p.run()
	return nil
}

func (p *program) Stop(s service.Service) error {
	// Graceful shutdown logic
	fmt.Printf("Stopped called from *p program")
	return nil
}

// ============================================================================
// SECTION 3: PIPE SERVER AND CONNECTION HANDLING
// ============================================================================

func (p *program) run() {
	p.queue = make(chan models.NoteRequest, 100)
	p.failedQueue = make(chan models.NoteRequest, 100)

	noteManager := notes.NewNoteManager(p.queue, p.failedQueue, "http://localhost:8080/api/notes")
	notes.SetNoteManager(noteManager)

	noteManager.Initialize()

	config := &winio.PipeConfig{
		SecurityDescriptor: "D:P(A;;GA;;;AU)", // Allows Authenticated Users to write to the pipe
	}

	l, err := winio.ListenPipe(`\\.\pipe\mnote_pipe`, config)
	if err != nil {
		log.Fatal("Pipe Listen Error:", err) // This will now show up in Event Viewer if it fails
	}
	defer l.Close()

	for {
		conn, err := l.Accept()
		if err != nil {
			continue
		}
		go p.handleConnection(conn)
	}
}

func (p *program) handleConnection(conn net.Conn) {
	s, _ := getService()
	logger, _ := s.Logger(nil)

	defer conn.Close()
	scanner := bufio.NewScanner(conn)

	noteManager := notes.GetNoteManager()

	for scanner.Scan() {
		rawBytes := scanner.Bytes()

		var receivedNote models.NoteRequest

		err := json.Unmarshal(rawBytes, &receivedNote)
		if err != nil {
			log.Printf("Error decoding JSON: %v. Raw data: %s", err, string(rawBytes))
			continue
		}

		log.Printf("\nReceived Note: %+v\n", receivedNote)

		if err := noteManager.ProcessNote(receivedNote); err != nil {
			logger.Info("Error processing note: %v\n", err)
			continue
		}
	}

}

// ============================================================================
// SECTION 4: COBRA COMMANDS - SERVICE MANAGEMENT
// ============================================================================

var startCmd = &cobra.Command{
	Use:   "start",
	Short: "Starts the mnote daemon",
	Run: func(cmd *cobra.Command, args []string) {
		s, _ := getService()
		err := s.Start()
		if err != nil {
			fmt.Printf("Failed to start: %s\n", err)
			return
		}
		fmt.Println("Service started.")
	},
}

var stopCmd = &cobra.Command{
	Use:   "stop",
	Short: "Stops the mnote daemon",
	Run: func(cmd *cobra.Command, args []string) {
		s, _ := getService()
		err := s.Stop()
		if err != nil {
			fmt.Printf("Failed to stop: %s\n", err)
			return
		}
		fmt.Println("Service stopped.")
	},
}

var serverCmd = &cobra.Command{
	Use: "server",
	Run: func(cmd *cobra.Command, args []string) {
		s, _ := getService()
		if err := s.Run(); err != nil {
			log.Fatal(err)
		}
	},
}

var installCmd = &cobra.Command{
	Use:   "install",
	Short: "Registers the mnote daemon with Windows SCM",
	Run: func(cmd *cobra.Command, args []string) {
		s, _ := getService()
		err := s.Install()
		if err != nil {
			fmt.Printf("Failed to install: %s\n", err)
			return
		}
		fmt.Println("Service installed successfully.")
	},
}

var uninstallCmd = &cobra.Command{
	Use:   "uninstall",
	Short: "Removes the mnote daemon from Windows SCM",
	Run: func(cmd *cobra.Command, args []string) {
		s, err := getService()
		if err != nil {
			fmt.Printf("Initialization error: %s\n", err)
			return
		}

		// 1. Stop the service first (best practice)
		s.Stop()

		// 2. Remove from Windows Registry
		err = s.Uninstall()
		if err != nil {
			fmt.Printf("Failed to uninstall: %s\n", err)
			return
		}
		fmt.Println("Service uninstalled successfully.")
	},
}

// ============================================================================
// SECTION 5: COBRA COMMANDS - CLIENT OPERATIONS
// ============================================================================

var rootCmd = &cobra.Command{
	Use:   "mnote",
	Short: "-- To be added -- ",
	Long:  `-- To be added -- `,
	Run:   func(cmd *cobra.Command, args []string) {},
}

var addCmd = &cobra.Command{
	Use:   "add",
	Short: "-- To be added -- ",
	Long:  `-- To be added -- `,
	Args:  cobra.ArbitraryArgs,
	Run:   addCmdRun,
}

func addCmdRun(cmd *cobra.Command, args []string) {
	// future scope : to handle note/text which contains "-"
	// The "Interspersed" Problem, options : The POSIX Terminator (--) or Disable Interspersed Flags or handle manually writing parsing loops

	if len(args) == 0 {
		fmt.Println("Error, please provide note content")
	}

	fmt.Printf("tags: %v\n", tagList)

	note := models.NoteRequest{
		Id:         uuid.New().String(),
		Content:    strings.Join(args, " "),
		Tags:       utils.CleanNoteTags(tagList),
		Importance: utils.ParseNoteImportance(importance),
		CreatedAt:  time.Now().Format(time.RFC3339),
	}

	payload, _ := json.Marshal(note)

	// Use winio.DialPipe for native Windows Pipe handling
	conn, err := winio.DialPipe(`\\.\pipe\mnote_pipe`, nil)
	if err != nil {
		fmt.Println("Error: Daemon not running.")
		return
	}
	defer conn.Close()

	// --- Sending to Daemon here ---
	// appending a \n because Daemon's bufio.Scanner looks for it
	// to know the message is complete.
	fmt.Fprintf(conn, "%s\n", string(payload))
}

// ============================================================================
// SECTION 6: INITIALIZATION AND ENTRY POINT
// ============================================================================

func Execute() {
	err := rootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {
	rootCmd.AddCommand(installCmd)
	rootCmd.AddCommand(uninstallCmd)
	rootCmd.AddCommand(startCmd, stopCmd)
	rootCmd.AddCommand(serverCmd)
	rootCmd.AddCommand(addCmd)

	rootCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
	addCmd.Flags().StringSliceVarP(&tagList, "tags", "t", []string{}, "Tags for the note (e.g. #work #todo)")
	addCmd.Flags().StringVarP(&importance, "imp", "i", "", "Importance level (*, **, or ***)")
}
