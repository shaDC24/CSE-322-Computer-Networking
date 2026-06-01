# Socket Programming - File Server Assignment

## Overview
This is a multi-threaded file server system where clients can upload, download, and request files from each other. The server manages client directories, file access control, and maintains history logs.

## Architecture

### Server.java
- Main server class that listens for client connections on port 6666
- Maintains synchronized data structures for connected clients and registered users
- Manages configuration parameters:
  - **MAX_BUFFER_SIZE**: Maximum total size of files in buffer (10 MB)
  - **MIN_CHUNK_SIZE**: Minimum chunk size for file transmission (1 KB)
  - **MAX_CHUNK_SIZE**: Maximum chunk size for file transmission (100 KB)
- Provides synchronized methods for:
  - Registering/unregistering clients
  - Checking buffer capacity
  - Generating unique file IDs
  - Sending messages to specific clients or broadcasting

### ClientHandler.java
- Extends Thread - handles each connected client in a separate thread
- Manages authentication and user registration
- Creates client directory structure
- Handles various client requests:
  - `LIST_USERS`: Display all registered users with online/offline status
  - `LIST_FILES`: Display user's own files (private and public)
  - `LIST_PUBLIC_FILES`: Display public files of other users
  - `UPLOAD`: Upload files with access control
  - `DOWNLOAD`: Download files from other users
  - `FILE_REQUEST`: Request files from specific users or broadcast
  - `VIEW_MESSAGES`: View incoming file requests
  - `VIEW_HISTORY`: View upload/download history
  - `LOGOUT`: Graceful disconnection
- Maintains upload sessions with chunk tracking
- Manages file request inbox

### Client.java
- Client application that connects to the server
- Authentication with username
- Interactive menu-driven interface
- Spawns a message receiver thread to listen for server responses
- Supports all server operations through command interface
- File upload/download capabilities

## Key Features

### 1. Client Authentication & Registration
- Each client must provide a unique username
- Duplicate login attempts are rejected
- Server maintains list of all registered users

### 2. Client Directories
- Server automatically creates a directory for each client on first connection
- Directory format: `ClientFiles/<username>`

### 3. File Operations
- **Upload**: Client splits files into chunks based on server-specified chunk size
  - Buffer size validation before acceptance
  - Acknowledgment protocol for each chunk
  - Completion verification with file size matching
- **Download**: Server sends file in MAX_CHUNK_SIZE chunks
  - No acknowledgment required from receiver
  - Completion message sent after transfer
- **Request**: Unicast to specific user or broadcast to all users

### 4. File Access Control
- Files can be marked as private or public
- Files uploaded in response to requests are automatically public
- Clients can only download public files from other users

### 5. Message System
- Clients receive notifications when their requested files are uploaded
- Multiple clients can upload the same requested file
- Unread messages displayed to client

### 6. History Logging
- Upload/download history maintained for each client
- Logged with: filename, timestamp, action type, status

## Protocol Flow

### Upload Process
1. Client sends: filename, filesize, access type
2. Server validates buffer size and responds with fileID + chunkSize
3. Client splits file and sends chunks sequentially
4. Server acknowledges each chunk
5. Client sends completion message
6. Server verifies total size and confirms success/failure

### Download Process
1. Client requests file
2. Server sends file in MAX_CHUNK_SIZE chunks (no acknowledgment needed)
3. Server sends completion message

### File Request Process
1. Client creates request with description and recipient
2. Server generates request ID
3. Server sends request to recipient (unicast or broadcast)
4. Recipient can view request in messages
5. When recipient uploads matching file, requester is notified

## Threading Model
- Main server thread accepts connections in a loop
- Each client connection spawns a ClientHandler thread
- Message receiver thread in client listens for server responses
- Synchronized data structures protect concurrent access to shared state

## File Structure
```
SocketProgramming/
├── Server.java           # Main server class
├── ClientHandler.java    # Client connection handler
├── Client.java           # Client application
├── README.md            # This file
└── ClientFiles/         # Created at runtime for each user
    ├── username1/
    ├── username2/
    └── ...
```

## Running the Application

### Terminal 1 - Start Server
```bash
javac SocketProgramming/*.java
java SocketProgramming.Server
```

### Terminal 2+ - Start Clients
```bash
java SocketProgramming.Client
```

## Implementation Notes

### TODO Items in Skeleton Code
1. **handleListFiles()**: Implement listing user's files with private/public indicators
2. **handleListPublicFiles()**: Implement listing public files of other users
3. **handleDownloadRequest()**: Implement file download with chunk transmission
4. **handleFileRequest()**: Implement file request creation and distribution
5. **handleViewHistory()**: Implement history log reading and display
6. **Chunk transmission**: Implement actual file chunk reading/writing
7. **History logging**: Implement log file creation and updates
8. **Cleanup logic**: Implement temporary file cleanup on disconnection

### Important Considerations
- All client-server communication uses ObjectInputStream/ObjectOutputStream
- Buffer size must be validated before accepting uploads
- Incomplete uploads must be cleaned up when client disconnects
- File requests must generate unique request IDs
- History logs should be persistent on disk
- Multiple clients must be handled concurrently without race conditions
