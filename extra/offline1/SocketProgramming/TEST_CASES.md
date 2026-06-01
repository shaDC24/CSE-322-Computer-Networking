# Socket Programming File Server - Test Cases

## Test Suite Overview
This document contains all test cases for the Socket Programming File Server implementation.

---

## Test Case 1: Server Startup
**Objective:** Verify that the server starts correctly on port 6666

**Steps:**
1. Run `java SocketProgramming.Server`
2. Verify console output shows:
   - "Server started on port 6666"
   - Configuration parameters displayed
   - "Waiting for connection..."

**Expected Result:** Server starts successfully and waits for connections

---

## Test Case 2: Single Client Connection and Authentication
**Objective:** Verify client can connect and authenticate

**Steps:**
1. Start server
2. Start client with `java SocketProgramming.Client`
3. When prompted, enter username: "testuser1"
4. Verify response: "SUCCESS: Authenticated as testuser1"

**Expected Result:** Client connects successfully and authenticates

---

## Test Case 3: Duplicate Username Prevention
**Objective:** Verify server prevents duplicate usernames from connecting simultaneously

**Steps:**
1. Start server
2. Client A: Connect and authenticate as "testuser1"
3. Client B: Try to connect and authenticate as "testuser1"
4. Verify Client B receives: "ERROR: Username already connected or invalid format"

**Expected Result:** Duplicate connection rejected

---

## Test Case 4: Multiple Clients Connection
**Objective:** Verify multiple clients with different usernames can connect

**Steps:**
1. Start server
2. Client A: Connect as "alice"
3. Client B: Connect as "bob"
4. Client C: Connect as "charlie"
5. Each client executes: LIST_USERS

**Expected Result:** All three clients connect successfully and see each other in the user list with [ONLINE] status

---

## Test Case 5: LIST_USERS Command
**Objective:** Verify LIST_USERS command shows all registered and connected users

**Steps:**
1. Server started with 3 connected clients: alice, bob, charlie
2. Client alice executes: LIST_USERS
3. Verify output shows:
   ```
   USERS_LIST:
   alice [ONLINE]
   bob [ONLINE]
   charlie [ONLINE]
   ```

**Expected Result:** All users listed with correct online/offline status

---

## Test Case 6: Client Directory Creation
**Objective:** Verify client directory is created automatically

**Steps:**
1. Connect client as "newuser"
2. Verify directory "ClientFiles/newuser" is created
3. List directory contents

**Expected Result:** Directory structure created: ClientFiles/newuser/

---

## Test Case 7: Upload Request - Valid File
**Objective:** Verify upload request generates correct session

**Steps:**
1. Create test file: "testfile.txt" (5 KB)
2. Client connects as "user1"
3. Execute: UPLOAD testfile.txt
4. When prompted: Is file public? yes
5. Verify response format: "UPLOAD_CONFIRM:fileID:chunkSize"

**Expected Result:** Server generates valid fileID and random chunkSize (between 1KB-100KB)

---

## Test Case 8: Upload Request - File Size Exceeds Buffer
**Objective:** Verify server rejects files that exceed buffer limit

**Steps:**
1. Create large file > 10 MB
2. Client connects as "user2"
3. Execute: UPLOAD largeFile.bin
4. Verify response: "ERROR: Buffer size exceeded"

**Expected Result:** Upload rejected with buffer exceeded error

---

## Test Case 9: Download Request - Invalid Metadata
**Objective:** Verify server handles invalid download metadata

**Steps:**
1. Client connects as "user3"
2. Execute: DOWNLOAD (incomplete command)
3. Verify response: "ERROR: Invalid download metadata"

**Expected Result:** Proper error handling for malformed requests

---

## Test Case 10: File Request
**Objective:** Verify file request creation

**Steps:**
1. Client A (alice) connects
2. Client B (bob) connects
3. Alice executes: FILE_REQUEST "urgent_document.pdf" bob
4. Bob executes: VIEW_MESSAGES
5. Verify message appears in Bob's queue

**Expected Result:** File request message delivered and visible

---

## Test Case 11: List Public Files
**Objective:** Verify listing public files of another user

**Steps:**
1. Client alice uploads file as public
2. Client bob executes: LIST_PUBLIC_FILES alice
3. Verify list of alice's public files displayed

**Expected Result:** Public files listed correctly

---

## Test Case 12: Client Disconnection and Cleanup
**Objective:** Verify proper cleanup on client disconnect

**Steps:**
1. Client connects as "tempuser"
2. Upload incomplete file
3. Execute: LOGOUT
4. Verify console shows: "Client tempuser disconnected"
5. Try to list users - tempuser shows [OFFLINE]

**Expected Result:** Client properly unregistered and cleanup performed

---

## Test Case 13: View Messages (Empty)
**Objective:** Verify VIEW_MESSAGES when no pending requests

**Steps:**
1. New client connects
2. Execute: VIEW_MESSAGES
3. Verify response: "MESSAGES:\nNo unread messages\n"

**Expected Result:** Proper response for empty message queue

---

## Test Case 14: Unknown Command
**Objective:** Verify server handles unknown commands gracefully

**Steps:**
1. Client connects
2. Execute: INVALID_COMMAND
3. Verify response: "ERROR: Unknown command: INVALID_COMMAND"

**Expected Result:** Proper error message for unknown commands

---

## Test Case 15: Concurrent Uploads
**Objective:** Verify multiple clients can upload simultaneously

**Steps:**
1. Client A: Initiate upload of file1.txt
2. Client B: Initiate upload of file2.txt
3. Client C: Initiate upload of file3.txt
4. Verify all receive unique fileIDs and chunk sizes

**Expected Result:** All uploads processed independently with unique IDs

---

## Test Case 16: Chunk Size Randomization
**Objective:** Verify chunk sizes are random within range

**Steps:**
1. Same client uploads 5 different files
2. Observe 5 UPLOAD_CONFIRM messages with different chunkSizes
3. Verify all chunkSizes are between MIN_CHUNK_SIZE (1KB) and MAX_CHUNK_SIZE (100KB)

**Expected Result:** Chunk sizes vary and stay within specified range

---

## Test Case 17: Upload with Request ID
**Objective:** Verify upload as response to request includes request ID

**Steps:**
1. Client alice requests file from bob (generates requestID)
2. Bob uploads file as response with requestID
3. Verify metadata includes: filename:filesize:true:requestID

**Expected Result:** Request ID properly tracked with response upload

---

## Test Case 18: Empty Username
**Objective:** Verify server rejects empty usernames

**Steps:**
1. Start client
2. When prompted for username, press Enter (empty input)
3. Verify rejection: "Username cannot be empty"

**Expected Result:** Empty usernames rejected

---

## Test Case 19: List Files (Private & Public)
**Objective:** Verify listing user's own files

**Steps:**
1. Client uploads 2 private files and 2 public files
2. Execute: LIST_FILES
3. Verify response shows all 4 files with privacy status

**Expected Result:** All files listed with correct privacy flags

---

## Test Case 20: Stress Test - Multiple Operations
**Objective:** Verify server handles sustained load

**Steps:**
1. 5 clients connected simultaneously
2. Each client executes:
   - LIST_USERS (10 times)
   - UPLOAD requests (5 times)
   - FILE_REQUEST (3 times)
   - VIEW_MESSAGES (2 times)
3. Monitor server console for errors

**Expected Result:** No errors, all operations complete successfully

---

## Running the Tests

### Manual Testing:
```bash
# Terminal 1: Start server
cd /path/to/SocketProgramming
java SocketProgramming.Server

# Terminal 2+: Start clients
java SocketProgramming.Client
```

### Automated Testing Script:
Use the provided `test_runner.sh` for automated test execution.

---

## Configuration Parameters Being Tested:
- MAX_BUFFER_SIZE: 10 MB
- MIN_CHUNK_SIZE: 1 KB
- MAX_CHUNK_SIZE: 100 KB
- PORT: 6666

---

## Notes:
- Tests should be run in sequence to avoid port conflicts
- Ensure sufficient delay between client connections
- Monitor both server and client console outputs
- Check for proper thread handling and cleanup
