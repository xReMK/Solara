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

	updateAddTags    []string
	updateRemoveTags []string
	updateContent    string
	updateImportance string
)

type program struct {
	queue       chan models.NoteAddRequest
	failedQueue chan models.NoteAddRequest
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
	p.queue = make(chan models.NoteAddRequest, 100)
	p.failedQueue = make(chan models.NoteAddRequest, 100)

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
		var env models.Envelope
		if err := json.Unmarshal(rawBytes, &env); err != nil {
			log.Printf("Error decoding JSON: %v. Raw data: %s", err, string(rawBytes))
			continue
		}
		log.Printf("daemon : Raw Payload: %s\n", string(env.Payload))

		response := "Success: Note added."
		if err := noteManager.ProcessNote(env); err != nil {
			logger.Info("daemon : Error processing note by Spring: %v\n", err)
			response = fmt.Sprintf("Error: %v", err)
			continue
		}

		fmt.Fprintln(conn, response) // Send this back to the CLI
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

	note := models.NoteAddRequest{
		Id:         uuid.New().String(),
		Content:    strings.Join(args, " "),
		Tags:       utils.CleanNoteTags(tagList),
		Importance: utils.ParseNoteImportance(importance),
		CreatedAt:  time.Now().Format(time.RFC3339),
	}

	// Use winio.DialPipe for native Windows Pipe handling
	conn, err := winio.DialPipe(`\\.\pipe\mnote_pipe`, nil)
	if err != nil {
		fmt.Println("Error: Daemon not running.")
		return
	}
	defer conn.Close()

	payload, _ := json.Marshal(note)
	envelope := models.Envelope{
		Action:  models.ActionAdd,
		Payload: payload,
	}

	finalBytes, _ := json.Marshal(envelope)
	// --- Sending to Daemon here ---
	// appending a \n because Daemon's bufio.Scanner looks for it
	// to know the message is complete.
	fmt.Fprintln(conn, string(finalBytes))

	listenToResponse(conn)
}

var updateCmd = &cobra.Command{
	Use:   "update [id] [content]",
	Short: "-- To be added -- ",
	Long:  `-- To be added -- `,
	Args:  cobra.ArbitraryArgs,
	Run:   updateCmdRun,
}

func updateCmdRun(cmd *cobra.Command, args []string) {
	id := args[0]
	updateReq := models.NoteUpdateRequest{ID: id}

	if cmd.Flags().Changed("content") {
		contentVal := strings.Join(args[1:], " ")
		updateReq.Content = &contentVal
	}

	if cmd.Flags().Changed("add-tags") {
		tagsVal := utils.CleanNoteTags(updateAddTags)
		updateReq.AddTags = &tagsVal
	}

	if cmd.Flags().Changed("remove-tags") {
		tagsVal := utils.CleanNoteTags(updateRemoveTags)
		updateReq.RemoveTags = &tagsVal
	}

	if cmd.Flags().Changed("imp") {
		impVal := strings.Count(importance, "*")
		updateReq.Importance = &impVal
	}
	// If only 'imp' was changed, JSON is: {"id":"...", "importance":3}

	payload, _ := json.Marshal(updateReq)
	envelope := models.Envelope{
		Action:  models.ActionUpdate,
		Payload: payload,
	}

	conn, err := winio.DialPipe(`\\.\pipe\mnote_pipe`, nil)
	if err != nil {
		fmt.Println("Error: Daemon not running.")
		return
	}
	defer conn.Close()

	finalBytes, _ := json.Marshal(envelope)
	fmt.Fprintln(conn, string(finalBytes))

	//send to spring via daemon -- how does daemon identify the request type?
}

func listenToResponse(conn net.Conn) {
	conn.SetReadDeadline(time.Now().Add(5 * time.Second))

	scanner := bufio.NewScanner(conn)
	if scanner.Scan() {
		response := scanner.Text()
		fmt.Printf("Daemon Response: %s\n", response)
	} else if err := scanner.Err(); err != nil {
		fmt.Printf("Error waiting for daemon: %v\n", err)
	}
}

/*
How cmd.Flags().Changed() Works
	In Cobra, every flag has a default value
	If you define --imp with a default of 0, and the user doesn't type it, the variable still holds 0
	This makes it impossible to know if the user wanted to set importance to zero or just didn't care about it
	Changed(name) looks at the internal state of the flag set to see if that specific flag was actually present in the command line string

Limitation of standard Go structs
	Zero Values
	In Go, an int is 0, a string is "", and a bool is false
	When you json.Marshal(note), Go can't tell if you meant "Set importance to 0" or "I didn't touch importance
*/

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
	rootCmd.AddCommand(updateCmd)

	rootCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
	addCmd.Flags().StringSliceVarP(&tagList, "tags", "t", []string{}, "Tags for the note (e.g. #work #todo)")
	addCmd.Flags().StringVarP(&importance, "imp", "i", "", "Importance level (*, **, or ***)")
	updateCmd.Flags().StringSliceVarP(&updateAddTags, "add-tags", "a", []string{}, "Adding tags to an existing note")
	updateCmd.Flags().StringSliceVarP(&updateRemoveTags, "remove-tags", "r", []string{}, "Removing tags of an existing note")
	updateCmd.Flags().StringVarP(&updateContent, "content", "c", "", "Update note content")
	updateCmd.Flags().StringVarP(&updateImportance, "imp", "i", "", "Update importance level (*, **, or ***)")
}
