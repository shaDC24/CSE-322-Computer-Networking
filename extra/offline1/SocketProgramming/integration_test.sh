#!/bin/bash

# Socket Programming - Interactive Test Scenarios
# This script runs various test scenarios with actual client-server interaction

WORKSPACE="/media/shatabdi/New Volume/D drive/Level3 term 2/CSE 322/computer networking/Assignment-1/offline1/SocketProgramming"
cd "$WORKSPACE"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

LOG_FILE="integration_test_results.log"
> "$LOG_FILE"

TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

log_msg() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

test_case() {
    local name="$1"
    TESTS_RUN=$((TESTS_RUN + 1))
    log_msg "\n${CYAN}[TEST $TESTS_RUN] $name${NC}"
}

pass() {
    TESTS_PASSED=$((TESTS_PASSED + 1))
    log_msg "${GREEN}✓ PASSED${NC}"
}

fail() {
    TESTS_FAILED=$((TESTS_FAILED + 1))
    log_msg "${RED}✗ FAILED: $1${NC}"
}

# Create test files
setup_test_files() {
    log_msg "${BLUE}Setting up test files...${NC}"
    mkdir -p test_data
    
    # Small test file (5 KB)
    dd if=/dev/zero of=test_data/small.txt bs=1024 count=5 2>/dev/null
    echo "This is a test file content" >> test_data/small.txt
    
    # Medium test file (500 KB)
    dd if=/dev/zero of=test_data/medium.bin bs=1024 count=500 2>/dev/null
    
    # Large test file (test buffer limit)
    dd if=/dev/zero of=test_data/large.bin bs=1M count=12 2>/dev/null
    
    log_msg "${GREEN}Test files created:${NC}"
    ls -lh test_data/
}

# Test 1: Server Startup
test_case "Server Startup on Port 6666"
log_msg "Starting server..."
timeout 2 java -cp . SocketProgramming.Server > server_test.log 2>&1 &
SERVER_PID=$!
sleep 1

if ps -p $SERVER_PID > /dev/null 2>&1; then
    pass
    kill $SERVER_PID 2>/dev/null
else
    fail "Server failed to start"
fi
wait $SERVER_PID 2>/dev/null || true

# Test 2: Client Connection
test_case "Client Connection (Single Client)"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Connecting client..."
timeout 3 bash -c 'echo "testuser1" | java -cp . SocketProgramming.Client' > client_test.log 2>&1 || true

if grep -q "SUCCESS" client_test.log; then
    pass
    log_msg "Client output:"
    head -5 client_test.log | sed 's/^/  /'
else
    fail "Client authentication failed"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true
sleep 1

# Test 3: Multiple Clients Connection
test_case "Multiple Clients Connection Simultaneously"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Connecting 3 clients..."
for i in 1 2 3; do
    (timeout 2 bash -c "echo \"user$i\nLIST_USERS\n\" | java -cp . SocketProgramming.Client" > client_${i}_test.log 2>&1) &
    sleep 0.5
done

wait
sleep 1

# Check if all clients connected
if grep -q "SUCCESS" client_1_test.log && grep -q "SUCCESS" client_2_test.log && grep -q "SUCCESS" client_3_test.log; then
    pass
    log_msg "All 3 clients authenticated successfully"
else
    fail "Not all clients authenticated"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true
sleep 1

# Test 4: Directory Creation
test_case "Client Directory Creation"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Connecting client newuser..."
timeout 2 bash -c "echo \"newuser\" | java -cp . SocketProgramming.Client" > /dev/null 2>&1 || true

sleep 1
if [ -d "ClientFiles/newuser" ]; then
    pass
    log_msg "Directory created: ClientFiles/newuser"
else
    fail "Client directory not created"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true

# Test 5: LIST_USERS Command
test_case "LIST_USERS Command Execution"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > server.log 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Sending LIST_USERS command..."
timeout 3 bash -c 'echo -e "alice\nLIST_USERS\nLOGOUT" | java -cp . SocketProgramming.Client' > list_users_test.log 2>&1 || true

sleep 1
if grep -q "USERS_LIST" list_users_test.log; then
    pass
    log_msg "LIST_USERS response received"
else
    fail "LIST_USERS command failed"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true
sleep 1

# Test 6: Upload Command Parsing
test_case "Upload Request Processing"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Sending UPLOAD command..."
timeout 3 bash -c 'echo -e "uploaduser\nUPLOAD\ntest_data/small.txt\nyes\nLOGOUT" | java -cp . SocketProgramming.Client' > upload_test.log 2>&1 || true

sleep 1
if grep -q "UPLOAD_CONFIRM" upload_test.log; then
    pass
    UPLOAD_RESPONSE=$(grep "UPLOAD_CONFIRM" upload_test.log)
    log_msg "Upload response: $UPLOAD_RESPONSE"
else
    fail "Upload request failed"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true
sleep 1

# Test 7: Buffer Size Check
test_case "Buffer Size Validation"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Testing with oversized file (>10 MB)..."
timeout 3 bash -c 'echo -e "bufferuser\nUPLOAD\ntest_data/large.bin\nyes\nLOGOUT" | java -cp . SocketProgramming.Client' > buffer_test.log 2>&1 || true

sleep 1
if grep -q "ERROR.*Buffer" buffer_test.log; then
    pass
    log_msg "Buffer exceeded error received correctly"
else
    fail "Buffer check not working properly"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true

# Test 8: Chunk Size Randomization
test_case "Chunk Size Randomization Verification"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Initiating 5 uploads to verify chunk size variation..."
for i in {1..5}; do
    (timeout 2 bash -c "echo -e \"chunkuser$i\nUPLOAD\ntest_data/small.txt\nyes\nLOGOUT\" | java -cp . SocketProgramming.Client" > chunk_${i}_test.log 2>&1) &
    sleep 0.3
done

wait
sleep 1

# Extract chunk sizes
CHUNK_SIZES=$(grep -h "UPLOAD_CONFIRM" chunk_*_test.log 2>/dev/null | grep -oP ':\K[0-9]+$' | sort -u | wc -l)

if [ "$CHUNK_SIZES" -gt 1 ]; then
    pass
    log_msg "Found $CHUNK_SIZES different chunk sizes - randomization working"
else
    fail "Chunk size randomization not working properly"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true
sleep 1

# Test 9: LOGOUT Command
test_case "LOGOUT Command Execution"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > server_logout.log 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Testing LOGOUT sequence..."
timeout 3 bash -c 'echo -e "logoutuser\nLOGOUT" | java -cp . SocketProgramming.Client' > logout_test.log 2>&1 || true

sleep 1
if grep -q "GOODBYE" logout_test.log && grep -q "Client logoutuser disconnected" server_logout.log; then
    pass
    log_msg "LOGOUT processed correctly"
else
    fail "LOGOUT command failed"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true

# Test 10: VIEW_MESSAGES Command
test_case "VIEW_MESSAGES Command (Empty Queue)"
log_msg "Starting server..."
java -cp . SocketProgramming.Server > /dev/null 2>&1 &
SERVER_PID=$!
sleep 1

log_msg "Checking empty message queue..."
timeout 3 bash -c 'echo -e "msguser\nVIEW_MESSAGES\nLOGOUT" | java -cp . SocketProgramming.Client' > msg_test.log 2>&1 || true

sleep 1
if grep -q "No unread messages" msg_test.log; then
    pass
    log_msg "Empty message queue handled correctly"
else
    fail "MESSAGE handling failed"
fi

kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null || true

# Summary
log_msg "\n${YELLOW}════════════════════════════════════════${NC}"
log_msg "${YELLOW}INTEGRATION TEST SUMMARY${NC}"
log_msg "${YELLOW}════════════════════════════════════════${NC}"
log_msg "Total Tests Run: $TESTS_RUN"
log_msg "${GREEN}Passed: $TESTS_PASSED${NC}"
log_msg "${RED}Failed: $TESTS_FAILED${NC}"
log_msg "${YELLOW}════════════════════════════════════════${NC}"

# Cleanup
log_msg "\n${BLUE}Cleaning up processes...${NC}"
pkill -f "SocketProgramming" 2>/dev/null || true
sleep 1

if [ $TESTS_FAILED -eq 0 ]; then
    log_msg "\n${GREEN}ALL INTEGRATION TESTS PASSED!${NC} ✓"
    exit 0
else
    log_msg "\n${RED}$TESTS_FAILED TESTS FAILED!${NC} ✗"
    exit 1
fi
