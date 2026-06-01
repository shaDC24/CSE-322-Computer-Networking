#!/bin/bash

# Socket Programming File Server - Automated Test Runner
# This script runs comprehensive tests on the file server implementation

set -e

WORKSPACE="/media/shatabdi/New Volume/D drive/Level3 term 2/CSE 322/computer networking/Assignment-1/offline1/SocketProgramming"
cd "$WORKSPACE"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Log file
LOG_FILE="test_results.log"
> "$LOG_FILE"

log_message() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

test_case() {
    local test_name="$1"
    TESTS_RUN=$((TESTS_RUN + 1))
    log_message "\n${BLUE}[TEST $TESTS_RUN] $test_name${NC}"
}

pass_test() {
    TESTS_PASSED=$((TESTS_PASSED + 1))
    log_message "${GREEN}✓ PASSED${NC}"
}

fail_test() {
    local reason="$1"
    TESTS_FAILED=$((TESTS_FAILED + 1))
    log_message "${RED}✗ FAILED: $reason${NC}"
}

# Verify compilation
test_case "Java Files Compilation"
if javac *.java 2>&1 | grep -q "error"; then
    fail_test "Compilation errors found"
else
    if [ -f "Server.class" ] && [ -f "Client.class" ] && [ -f "ClientHandler.class" ]; then
        pass_test
    else
        fail_test "Class files not generated"
    fi
fi

# Test Case 2: Server Startup
test_case "Server Startup (Port 6666)"
timeout 5 java SocketProgramming.Server &
SERVER_PID=$!
sleep 2
if ps -p $SERVER_PID > /dev/null; then
    pass_test
    kill $SERVER_PID 2>/dev/null || true
else
    fail_test "Server failed to start"
fi

# Clean up before next tests
pkill -f "SocketProgramming.Server" || true
sleep 1

# Test Case 3: Configuration Parameters
test_case "Configuration Parameters Validation"
log_message "Checking Server.java for:"
log_message "  - MAX_BUFFER_SIZE: 10 MB"
log_message "  - MIN_CHUNK_SIZE: 1 KB"
log_message "  - MAX_CHUNK_SIZE: 100 KB"
log_message "  - PORT: 6666"

if grep -q "MAX_BUFFER_SIZE = 10 \* 1024 \* 1024" Server.java && \
   grep -q "MIN_CHUNK_SIZE = 1024" Server.java && \
   grep -q "MAX_CHUNK_SIZE = 1024 \* 100" Server.java && \
   grep -q "PORT = 6666" Server.java; then
    pass_test
else
    fail_test "Configuration parameters not correctly defined"
fi

# Test Case 4: Class Hierarchy and Method Existence
test_case "Class Structure Verification"
ISSUES=0

# Check Server class
if ! grep -q "public class Server" Server.java; then
    log_message "Missing Server class definition"
    ISSUES=$((ISSUES + 1))
fi

# Check ClientHandler class
if ! grep -q "public class ClientHandler extends Thread" ClientHandler.java; then
    log_message "ClientHandler doesn't extend Thread"
    ISSUES=$((ISSUES + 1))
fi

# Check Client class
if ! grep -q "public class Client" Client.java; then
    log_message "Missing Client class definition"
    ISSUES=$((ISSUES + 1))
fi

# Check key methods exist
METHODS=("registerClient" "unregisterClient" "getRegisteredUsers" "getConnectedClients" "authenticate" "handleClientRequest")
for method in "${METHODS[@]}"; do
    if ! grep -q "public.*$method" Server.java ClientHandler.java Client.java 2>/dev/null; then
        log_message "Missing method: $method"
        ISSUES=$((ISSUES + 1))
    fi
done

if [ $ISSUES -eq 0 ]; then
    pass_test
else
    fail_test "Found $ISSUES structural issues"
fi

# Test Case 5: Command Handler Implementation
test_case "Command Handler Verification"
COMMANDS=("LIST_USERS" "LIST_FILES" "UPLOAD" "DOWNLOAD" "FILE_REQUEST" "VIEW_MESSAGES" "LOGOUT")
MISSING=0

for cmd in "${COMMANDS[@]}"; do
    if ! grep -q "case \"$cmd\"" ClientHandler.java; then
        log_message "Missing handler for: $cmd"
        MISSING=$((MISSING + 1))
    fi
done

if [ $MISSING -eq 0 ]; then
    pass_test
else
    fail_test "Missing $MISSING command handlers"
fi

# Test Case 6: Authentication Flow
test_case "Authentication Flow Verification"
if grep -q "private boolean authenticate" ClientHandler.java && \
   grep -q "Server.registerClient" ClientHandler.java && \
   grep -q "String authMessage = " ClientHandler.java; then
    pass_test
else
    fail_test "Authentication flow not properly implemented"
fi

# Test Case 7: Directory Creation
test_case "Client Directory Creation Logic"
if grep -q "Files.createDirectories" ClientHandler.java && \
   grep -q "ClientFiles/" ClientHandler.java; then
    pass_test
else
    fail_test "Directory creation logic missing"
fi

# Test Case 8: Upload Session Management
test_case "Upload Session Management"
if grep -q "class UploadSession" ClientHandler.java && \
   grep -q "uploadSessions" ClientHandler.java && \
   grep -q "Map.*UploadSession" ClientHandler.java; then
    pass_test
else
    fail_test "Upload session management not properly implemented"
fi

# Test Case 9: File Request Queue
test_case "File Request Queue Implementation"
if grep -q "class FileRequest" ClientHandler.java && \
   grep -q "incomingRequests" ClientHandler.java && \
   grep -q "List.*FileRequest" ClientHandler.java; then
    pass_test
else
    fail_test "File request queue not properly implemented"
fi

# Test Case 10: Chunk Size Randomization
test_case "Random Chunk Size Generation"
if grep -q "generateRandomChunkSize" ClientHandler.java && \
   grep -q "new Random" ClientHandler.java && \
   grep -q "MIN_CHUNK_SIZE.*MAX_CHUNK_SIZE" ClientHandler.java; then
    pass_test
else
    fail_test "Chunk size randomization logic missing"
fi

# Test Case 11: Synchronization
test_case "Synchronization for Thread Safety"
SYNC_METHODS=("registerClient" "unregisterClient" "getRegisteredUsers" "getConnectedClients" "generateFileID")
UNSYNC=0

for method in "${SYNC_METHODS[@]}"; do
    if grep -q "public static synchronized.*$method" Server.java; then
        :
    else
        log_message "Not synchronized: $method"
        UNSYNC=$((UNSYNC + 1))
    fi
done

if [ $UNSYNC -eq 0 ]; then
    pass_test
else
    fail_test "Found $UNSYNC methods without synchronization"
fi

# Test Case 12: Stream Initialization
test_case "ObjectStream Initialization"
if grep -q "ObjectOutputStream" Client.java && \
   grep -q "ObjectInputStream" Client.java && \
   grep -q "ObjectOutputStream" ClientHandler.java && \
   grep -q "ObjectInputStream" ClientHandler.java; then
    pass_test
else
    fail_test "ObjectStream initialization missing"
fi

# Test Case 13: Error Handling
test_case "Exception Handling"
HANDLERS=("EOFException" "IOException" "ClassNotFoundException")
MISSING_HANDLERS=0

for handler in "${HANDLERS[@]}"; do
    if ! grep -q "catch.*$handler" Client.java ClientHandler.java; then
        log_message "Missing handler for: $handler"
        MISSING_HANDLERS=$((MISSING_HANDLERS + 1))
    fi
done

if [ $MISSING_HANDLERS -eq 0 ]; then
    pass_test
else
    fail_test "Missing $MISSING_HANDLERS exception handlers"
fi

# Test Case 14: Message Receiver Thread
test_case "Message Receiver Thread Implementation"
if grep -q "class MessageReceiver implements Runnable" Client.java && \
   grep -q "new Thread(new MessageReceiver())" Client.java; then
    pass_test
else
    fail_test "Message receiver thread not properly implemented"
fi

# Test Case 15: Menu Display
test_case "Menu Display Functionality"
if grep -q "displayMenu" Client.java && \
   grep -q "FILE SERVER MENU" Client.java; then
    pass_test
else
    fail_test "Menu display not implemented"
fi

# Clean up any leftover processes
pkill -f "SocketProgramming" || true

# Summary
log_message "\n${YELLOW}================================${NC}"
log_message "${YELLOW}TEST SUMMARY${NC}"
log_message "${YELLOW}================================${NC}"
log_message "Total Tests Run: $TESTS_RUN"
log_message "${GREEN}Passed: $TESTS_PASSED${NC}"
log_message "${RED}Failed: $TESTS_FAILED${NC}"
log_message "${YELLOW}================================${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    log_message "\n${GREEN}ALL TESTS PASSED!${NC} ✓"
    exit 0
else
    log_message "\n${RED}SOME TESTS FAILED!${NC} ✗"
    exit 1
fi
