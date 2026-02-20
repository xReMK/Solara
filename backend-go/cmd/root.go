/*
Copyright © 2026 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"bufio"
	"fmt"
	"log"
	"net"
	"os"
	"strings"

	"github.com/Microsoft/go-winio"
	"github.com/kardianos/service"
	"github.com/spf13/cobra"
)

// -------------------------------------- Windows Service Manager ------------------------------------------------

var logger service.Logger

type program struct{}

// Start is called by the SCM when the service starts
func (p *program) Start(s service.Service) error {
	// Must be non-blocking; run the Pipe Listener in a goroutine
	go p.run()
	return nil
}

func (p *program) run() {
	/*
		// Your Named Pipe / Daemon logic goes here
		// This is where the infinite loop lives
		// 1. Create the Named Pipe listener
		// In Go, named pipes on Windows are handled by the 'net' package
		// using the 'pipe' network type.
		l, err := net.Listen("unix", `\\.\pipe\mnote_pipe`)
		if err != nil {
			log.Fatal(err)
		}
		defer l.Close()

		for {
			// 2. Accept blocks the goroutine until a client (mnote add) connects
			conn, err := l.Accept()
			if err != nil {
				continue
			}

			// 3. Handle the connection in a goroutine (Parallel Processing)
			// Since you've worked with CUDA/Threads, you know we don't want
			// one command to block the entire pipe.
			go handleConnection(conn)
		}
	*/
	// Standard Windows Pipe configuration
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
		go handleConnection(conn)
	}
}

func handleConnection(conn net.Conn) {
	defer conn.Close()
	scanner := bufio.NewScanner(conn)
	for scanner.Scan() {
		data := scanner.Text()
		// Here: Push 'data' to your local Go Queue or POST to Spring Boot
		fmt.Printf("Received note: %s\n", data)
	}
}

func (p *program) Stop(s service.Service) error {
	// Graceful shutdown logic
	return nil
}

func getService() (service.Service, error) {
	//exePath, _ := os.Executable()
	svcConfig := &service.Config{
		Name:        "mnote",
		DisplayName: "mNote Daemon",
		Description: "Background note-taking service for Go/Spring project.",
		// Crucial: This tells Windows to run "mnote.exe server" on boot
		Arguments: []string{"server"},
		// Force the OS to use the absolute path to the binary
		Executable: `D:\Solara\backend-go\mnote.exe`, // Raw string,
	}

	prg := &program{}
	return service.New(prg, svcConfig)
}

var serverCmd = &cobra.Command{
	Use: "server",
	Run: func(cmd *cobra.Command, args []string) {
		s, _ := getService()

		// This is the crucial part.
		// s.Run() tells the Go program:
		// "Wait here and listen for Windows Service signals (Stop/Shutdown)."
		// It stays at this line for days/weeks until the PC shuts down.
		if err := s.Run(); err != nil {
			log.Fatal(err)
		}
	},
}

// mnote install
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

// mnote uninstall
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

// mnote start
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

// -------------------------------------- mnote commands ------------------------------------------------
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
	/*
		// 1. Join the arguments into a single string
		note := strings.Join(args, " ")

		// 2. Connect to the Named Pipe
		// On Windows, the pipe path is treated as a local address.
		pipePath := `\\.\pipe\mnote_pipe`

		// Dial times out if the server isn't running
		conn, err := net.Dial("unix", pipePath)
		if err != nil {
			fmt.Printf("Error: Daemon not running. (Run 'mnote start' first)\n")
			return
		}
		defer conn.Close()

		// 3. Write the data to the pipe
		// We add a newline so the server's bufio.Scanner knows where the message ends
		_, err = fmt.Fprintf(conn, "%s\n", note)
		if err != nil {
			fmt.Printf("Error sending data: %v\n", err)
			return
		}

		fmt.Println("Note sent to background daemon.")
	*/
	note := strings.Join(args, " ")

	// Use winio.DialPipe for native Windows Pipe handling
	conn, err := winio.DialPipe(`\\.\pipe\mnote_pipe`, nil)
	if err != nil {
		fmt.Println("Error: Daemon not running.")
		return
	}
	defer conn.Close()

	fmt.Fprintf(conn, "%s\n", note)
}

func Execute() {
	err := rootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {

	// Add all the modes to your tool
	rootCmd.AddCommand(installCmd)   // For registering
	rootCmd.AddCommand(uninstallCmd) // For uninstalling
	rootCmd.AddCommand(startCmd)     // For initial manual start
	rootCmd.AddCommand(serverCmd)    // THE IMPORTANT ONE: The OS calls this
	rootCmd.AddCommand(addCmd)       // The client tool you actually use

	rootCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
}
