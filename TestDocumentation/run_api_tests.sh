#!/bin/bash

echo "=========================================="
echo "  LOAD DISBURSEMENT SERVICE - API TEST"
echo "=========================================="
echo "Date: $(date)"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Test 1: Create Provider
echo "1. CREATE PROVIDER (Globe)"
echo "----------------------------"
PROVIDER_RESPONSE=$(curl -s -X POST http://localhost:8080/providers \
  -H "Content-Type: application/json" \
  -d '{"data":{"type":"providers","attributes":{"name":"Globe","feeAmount":10,"validateApiUrl":"http://wiremock:8080/validate","disbursementApiUrl":"http://wiremock:8080/disburse"}}}')
echo "$PROVIDER_RESPONSE"
PROVIDER_ID=$(echo "$PROVIDER_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | sed 's/"id":"//;s/"//')

if [ -n "$PROVIDER_ID" ]; then
  echo -e "${GREEN}PASS${NC} - Provider created with ID: $PROVIDER_ID"
else
  echo -e "${RED}FAIL${NC} - Provider creation failed"
  exit 1
fi

echo ""

# Test 2: Get All Providers
echo "2. GET ALL PROVIDERS"
echo "---------------------"
PROVIDERS=$(curl -s http://localhost:8080/providers)
echo "$PROVIDERS"

if echo "$PROVIDERS" | grep -q '"data":\['; then
  echo -e "${GREEN}PASS${NC} - Providers list retrieved"
else
  echo -e "${RED}FAIL${NC} - Failed to get providers"
  exit 1
fi

echo ""

# Test 3: Create Load Order
echo "3. CREATE LOAD ORDER (100 pesos)"
echo "---------------------------------"
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/orders/load \
  -H "Content-Type: application/json" \
  -d "{\"data\":{\"type\":\"orders\",\"attributes\":{\"providerId\":\"$PROVIDER_ID\",\"accountNumber\":\"+639123456789\",\"amount\":100}}}")
echo "$ORDER_RESPONSE"
ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | sed 's/"id":"//;s/"//')

if echo "$ORDER_RESPONSE" | grep -q '"status":"NEW"'; then
  echo -e "${GREEN}PASS${NC} - Order created with ID: $ORDER_ID, Status: NEW"
else
  echo -e "${RED}FAIL${NC} - Order creation failed"
  exit 1
fi

echo ""

# Test 4: Disburse Order
echo "4. DISBURSE ORDER"
echo "------------------"
DISBURSE_RESPONSE=$(curl -s -X POST "http://localhost:8080/orders/load/$ORDER_ID" \
  -H "Content-Type: application/json" \
  -d '{"data":{"type":"orders","attributes":{"paymentId":"550e8400-e29b-41d4-a716-446655440002"}}}')
echo "$DISBURSE_RESPONSE"

if echo "$DISBURSE_RESPONSE" | grep -q '"status":"SUCCESS"'; then
  echo -e "${GREEN}PASS${NC} - Order disbursed, Status: SUCCESS"
elif echo "$DISBURSE_RESPONSE" | grep -q '"status":"FAILED"'; then
  echo -e "${GREEN}PASS${NC} - Order disbursed, Status: FAILED (partner API declined)"
else
  echo -e "${RED}FAIL${NC} - Disbursement failed"
  exit 1
fi

echo ""

# Test 5: Get Order Details
echo "5. GET ORDER DETAILS"
echo "---------------------"
ORDER_DETAILS=$(curl -s "http://localhost:8080/orders/$ORDER_ID")
echo "$ORDER_DETAILS"

if echo "$ORDER_DETAILS" | grep -q '"id":"'$ORDER_ID'"'; then
  echo -e "${GREEN}PASS${NC} - Order details retrieved"
else
  echo -e "${RED}FAIL${NC} - Failed to get order details"
  exit 1
fi

echo ""
echo "=========================================="
echo "  ALL API TESTS PASSED!"
echo "=========================================="
