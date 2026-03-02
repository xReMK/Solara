# mNote Service Architecture Guide

## Table of Contents
1. [Service Lifecycle Overview](#service-lifecycle-overview)
2. [Key Components](#key-components)
3. [s.Start() vs s.Run() Explained](#sstart-vs-srun-explained)
4. [Two Execution Paths](#two-execution-paths)
5. [p.Start() and p.Stop() Lifecycle](#pstart-and-pstop-lifecycle)
6. [Quick Reference](#quick-reference)

---

## Service Lifecycle Overview

### The Complete Flow

When you run `mnote server`, the following sequence occurs:

1. **serverCmd is called** (user runs: `mnote server`)
2. **s.Run() is invoked** (kardianos/service library)
3. **Service Library connects to Windows SCM** (Service Control Manager)
4. **Windows SCM sends START message**
5. **s.Run() internally calls p.Start()**
6. **p.Start() spawns go p.run()** (non-blocking goroutine)
7. **p.run() starts indefinite loop** listening on named pipe
8. **p.Start() returns immediately** (non-blocking)
9. **s.Run() keeps running** waiting for SCM messages
10. **When SCM sends STOP**: s.Run() calls p.Stop()
11. **p.Stop() executes cleanup logic**
12. **Service terminates**

---

## Key Components

### 1. `s.Run()` - The Bridge to Windows Service Manager

**What it is:**
- Entry point that connects your program to Windows SCM
- Acts as a mediator between your application and Windows Service Control Manager
- Blocks and waits for messages from Windows SCM

**Key characteristics:**
- Hands over control to the service library
- Calls your `p.Start()` and `p.Stop()` methods at appropriate times
- Keeps running indefinitely while the service is active

**Code location:**
```go
var serverCmd = &cobra.Command{
    Use: "server",
    Run: func(cmd *cobra.Command, args []string) {
        s, _ := getService()
        if err := s.Run(); err != nil {
            log.Fatal(err)
        }
    },
}
```

### 2. `p.Start()` - Service Start Hook

**What it is:**
- Lifecycle method called by the service library when SCM sends a START message
- Must return immediately (non-blocking)

**Why non-blocking:**
If `p.Start()` blocked, `s.Run()` couldn't listen to SCM messages

**Implementation:**
```go
func (p *program) Start(s service.Service) error {
    // Must be non-blocking; run the Pipe Listener in a goroutine
    go p.run()
    return nil
}
```

**Called by:** Windows Service Manager (via service library)  
**When:** When the service is being started

### 3. `p.Stop()` - Service Stop Hook

**What it is:**
- Lifecycle method called by the service library when SCM sends a STOP message
- Handles graceful shutdown logic

**Implementation:**
```go
func (p *program) Stop(s service.Service) error {
    // Graceful shutdown logic
    fmt.Printf("Stopped called from *p program")
    return nil
}
```

**Called by:** Windows Service Manager (via service library)  
**When:** When the service is being stopped or system is shutting down

### 4. `p.run()` - The Actual Server

**What it is:**
- The core server logic that actually does the work
- Runs in a background goroutine spawned by `p.Start()`
- Contains the indefinite loop listening for connections

**Key characteristics:**
- Creates named pipe listener: `\\.\pipe\mnote_pipe`
- Accepts connections in an infinite loop
- Spawns a new handler goroutine for each connection
- Runs independently while `s.Run()` monitors for SCM messages

**Implementation:**
```go
func (p *program) run() {
    p.queue = make(chan string, 100)
    
    config := &winio.PipeConfig{
        SecurityDescriptor: "D:P(A;;GA;;;AU)",
    }
    
    l, err := winio.ListenPipe(`\\.\pipe\mnote_pipe`, config)
    if err != nil {
        log.Fatal("Pipe Listen Error:", err)
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
```

---

## s.Start() vs s.Run() Explained

### Quick Comparison

| Aspect | `s.Start()` | `s.Run()` |
|--------|-----------|----------|
| **What it does** | Tells Windows SCM to start an *already-installed* service | Actually runs the service process |
| **Who calls it** | You (via `mnote start` command or Services.msc UI) | You (via `mnote server` command) OR Windows SCM automatically |
| **Prerequisites** | Service must be installed first (`mnote install`) | None (can run standalone) |
| **Blocks?** | No, returns immediately | Yes, continues running indefinitely |
| **Use case** | Production (auto-start on boot) | Development/Testing |

### `s.Start()` - The Command

**What it does:**
- Makes an HTTP/IPC request to Windows SCM
- SCM will then spawn a NEW process with the registered arguments
- Returns immediately to your terminal

**Code:**
```go
var startCmd = &cobra.Command{
    Use:   "start",
    Short: "Starts the mnote daemon",
    Run: func(cmd *cobra.Command, args []string) {
        s, _ := getService()
        err := s.Start()  // Just tells SCM to start the service
        if err != nil {
            fmt.Printf("Failed to start: %s\n", err)
            return
        }
        fmt.Println("Service started.")
    },
}
```

**Flow:**
```
mnote start
  ↓
Calls s.Start()
  ↓
Sends message to Windows SCM: "Start the 'mnote' service"
  ↓
Returns immediately (non-blocking)
  ↓
SCM spawns: mnote.exe server  (using Arguments from service config)
```

### `s.Run()` - The Server

**What it does:**
- Actually runs the service
- Connects to Windows SCM and says "I am ready"
- Waits for SCM messages indefinitely
- Maintains the server lifecycle

**Code:**
```go
var serverCmd = &cobra.Command{
    Use: "server",
    Run: func(cmd *cobra.Command, args []string) {
        s, _ := getService()
        if err := s.Run(); err != nil {  // Blocks here, waiting for SCM
            log.Fatal(err)
        }
    },
}
```

**Flow:**
```
mnote server
  ↓
Calls s.Run()
  ↓
Connects to Windows SCM
  ↓
Calls p.Start() (service library does this)
  ↓
p.Start() spawns go p.run() (non-blocking)
  ↓
s.Run() continues waiting for SCM messages (STOP, SHUTDOWN, etc.)
  ↓
When SCM sends STOP: s.Run() calls p.Stop()
```

---

## Two Execution Paths

### Path 1: Manual Execution (Development/Testing)

**When to use:**
- Development and debugging
- Testing the service locally
- Quick startup without Windows service registration

**Steps:**
```powershell
.\mnote.exe server
```

**What happens:**
1. serverCmd runs
2. s.Run() is called
3. Detects it's NOT running as a Windows service
4. Goes into standalone mode
5. Internally calls p.Start()
6. p.Start() spawns p.run()
7. p.run() listens on the named pipe
8. Service runs until you press Ctrl+C or an error occurs

**Advantages:**
- No installation required
- Can see output directly in terminal
- Easy to debug with console output
- Can modify and restart quickly

**Disadvantages:**
- Doesn't survive system restart
- Doesn't integrate with Windows Service Manager
- You have to manually start it every time

---

### Path 2: Automatic Service Execution (Production)

**When to use:**
- Production deployment
- Want auto-start on system boot
- Want SCM to manage lifecycle and restarts

**Steps:**

**Step 1: Install the service**
```powershell
.\mnote.exe install
```
This registers the service with Windows SCM and stores the Arguments in the registry.

**Step 2: Start the service (either way)**
```powershell
# Option A: Command line
.\mnote.exe start

# Option B: GUI (Services.msc)
# Search for "Services" and start "mNote Daemon"
```

**What happens:**
1. Windows SCM receives the START request
2. SCM reads the registered configuration from registry
3. Sees Arguments = "server"
4. SCM spawns a NEW process: `mnote.exe server`
5. s.Run() is called in the new process
6. Detects it's running as a Windows service
7. Calls p.Start()
8. p.Start() spawns p.run()
9. p.run() listens on the named pipe
10. SCM continuously monitors the process
11. If process crashes, SCM automatically restarts it

**Advantages:**
- Auto-starts on system boot (configurable via Services.msc)
- SCM monitors and manages the process
- Auto-restart on crash
- Integrates with Windows service infrastructure
- Can be controlled via Services.msc GUI or command line

**Disadvantages:**
- Requires installation step first
- Can't see output in terminal (must check Event Viewer or logs)
- More overhead

---

## p.Start() and p.Stop() Lifecycle

### When They Get Called

| Method | Called By | When | Context |
|--------|-----------|------|---------|
| `p.Start()` | Windows Service Manager (via service library) | After `s.Run()` is called and SCM sends START | Service startup |
| `p.Stop()` | Windows Service Manager (via service library) | When SCM sends STOP or system is shutting down | Service shutdown |

### Who Calls Whom

```
s.Run() (Running and waiting)
  ↓
Windows SCM sends START
  ↓
Service Library intercepts
  ↓
Calls p.Start()
  ↓
p.Start() spawns: go p.run()
  ↓
p.Start() returns immediately
  ↓
s.Run() continues to wait...

[Later, Windows stops the service]
  ↓
Windows SCM sends STOP
  ↓
Service Library intercepts
  ↓
Calls p.Stop()
  ↓
p.Stop() executes cleanup
  ↓
Service terminates
```

### Important Notes

**You Never Call Them Directly:**
- `p.Start()` and `p.Stop()` are NOT called by your code
- They are interface methods that the service library expects to find
- The Windows Service Manager (via the library) calls them automatically

**Non-Blocking Requirement:**
- `p.Start()` MUST NOT block
- If it blocked, `s.Run()` couldn't listen for SCM messages
- This is why we use `go p.run()` to spawn the listener in a background goroutine

**Graceful Shutdown:**
- `p.Stop()` is your opportunity to gracefully shutdown
- You should: close listeners, flush buffers, save state, etc.
- Currently it just prints a message, but you might want to enhance it

---

## Quick Reference

### Commands Cheat Sheet

```powershell
# Development/Testing
.\mnote.exe server              # Run service manually in terminal

# Production Setup
.\mnote.exe install             # Register service with Windows
.\mnote.exe start               # Start the service
.\mnote.exe stop                # Stop the service
.\mnote.exe uninstall           # Unregister service from Windows

# Client Operations
.\mnote.exe add "Your note"     # Send a note to the service
```

### Key Files and Configuration

**Service Configuration Location:**
```go
// in cmd/root.go - getService() function
svcConfig := &service.Config{
    Name:        "mnote",
    DisplayName: "mNote Daemon",
    Description: "Background note-taking service for Go/Spring project.",
    Arguments:   []string{"server"},  // ← This is critical!
    Executable:  `D:\Solara\backend-go\mnote.exe`,
}
```

**The Arguments Field is Critical:**
- Stored in Windows Registry after `mnote install`
- Tells SCM which command to run when starting the service
- Why we use `"server"` - so SCM runs: `mnote.exe server`

### Common Scenarios

**Scenario 1: I want to test the service locally**
```powershell
.\mnote.exe server
# Run directly in terminal, Ctrl+C to stop
```

**Scenario 2: I want the service to auto-start on boot**
```powershell
.\mnote.exe install           # First time only
.\mnote.exe start             # Start it now
# Go to Services.msc → Right-click mNote Daemon → Properties → Set to "Automatic"
```

**Scenario 3: I want to see what's happening while it's running**
```powershell
.\mnote.exe server            # See output in terminal
# OR check Event Viewer if running as service
```

**Scenario 4: The service crashed, but I don't know why**
```powershell
# Check Windows Event Viewer
# Settings → System → System and Security → View event logs
# Look for "mNote Daemon" messages
```

---

## Summary

**The key insight:**
- `s.Run()` = The service wrapper that integrates with Windows SCM
- `p.Start()` = Called by SCM to initialize your service
- `p.run()` = The actual server that does the real work
- `p.Stop()` = Called by SCM to shutdown your service

**You have two paths:**
1. **`mnote server`** = Run directly (no SCM, useful for development)
2. **`mnote install` + `mnote start`** = Register and run as Windows service (auto-start on boot)

Both paths call the same `p.Start()` and `p.run()` eventually, but the service path adds SCM integration which provides auto-restart and startup capabilities.

