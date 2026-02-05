# Load Disbursement Service - API Test Documentation

## Test Date: 2026-02-05

## Services Status

| Service | Container | Port | Status |
|---------|-----------|------|--------|
| Spring Boot App | disbursement-service | 8080 | Running |
| PostgreSQL | disbursement-postgres | 5432 | Healthy |
| Wiremock | disbursement-wiremock | 8081 | Running |

---

## API Test Results

### 1. POST /providers - Create Provider

**Request:**
```bash
curl -X POST http://localhost:8080/providers \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "type": "providers",
      "attributes": {
        "name": "Globe",
        "feeAmount": 10,
        "validateApiUrl": "http://wiremock:8080/validate",
        "disbursementApiUrl": "http://wiremock:8080/disburse"
      }
    }
  }'
```

**Response (201 Created):**
```json
{
  "data": {
    "type": "providers",
    "id": "b04ce6ce-2478-43fb-8832-39d8ad10b694",
    "attributes": {
      "name": "Globe",
      "feeAmount": 10,
      "validateApiUrl": "http://wiremock:8080/validate",
      "disbursementApiUrl": "http://wiremock:8080/disburse",
      "createdAt": "2026-02-05T14:07:53.794295842"
    }
  }
}
```

**Status: PASS**

---

### 2. GET /providers - List All Providers

**Request:**
```bash
curl http://localhost:8080/providers
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "type": "providers",
      "id": "46f07c62-60c4-446d-8bd0-52e9957c457c",
      "attributes": {
        "name": "Globe",
        "feeAmount": 10.00,
        "validateApiUrl": "http://wiremock:8080/validate",
        "disbursementApiUrl": "http://wiremock:8080/disburse",
        "createdAt": "2026-02-05T13:57:56.503168"
      }
    }
  ]
}
```

**Status: PASS**

---

### 3. POST /orders/load - Create Load Order

**Request:**
```bash
curl -X POST http://localhost:8080/orders/load \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "type": "orders",
      "attributes": {
        "providerId": "b04ce6ce-2478-43fb-8832-39d8ad10b694",
        "accountNumber": "+639123456789",
        "amount": 100
      }
    }
  }'
```

**Response (201 Created):**
```json
{
  "data": {
    "type": "orders",
    "id": "d97b0800-2be9-4a87-a3ff-f48bb6e736d3",
    "attributes": {
      "providerId": "b04ce6ce-2478-43fb-8832-39d8ad10b694",
      "accountNumber": "+639123456789",
      "providerName": "Globe",
      "baseAmount": 100,
      "feeAmount": 10.00,
      "totalAmount": 110.00,
      "status": "NEW",
      "createdAt": "2026-02-05T14:07:53.912851634",
      "updatedAt": "2026-02-05T14:07:53.912855676"
    }
  }
}
```

**Verification:**
- Base Amount: 100
- Fee Amount: 10 (from provider)
- Total Amount: 110 (100 + 10)
- Status: NEW

**Status: PASS**

---

### 4. POST /orders/load/{orderId} - Disburse Load Order

**Request:**
```bash
curl -X POST http://localhost:8080/orders/load/d97b0800-2be9-4a87-a3ff-f48bb6e736d3 \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "type": "orders",
      "attributes": {
        "paymentId": "550e8400-e29b-41d4-a716-446655440002"
      }
    }
  }'
```

**Response (200 OK):**
```json
{
  "data": {
    "type": "orders",
    "id": "d97b0800-2be9-4a87-a3ff-f48bb6e736d3",
    "attributes": {
      "providerId": "b04ce6ce-2478-43fb-8832-39d8ad10b694",
      "accountNumber": "+639123456789",
      "paymentId": "550e8400-e29b-41d4-a716-446655440002",
      "providerName": "Globe",
      "baseAmount": 100.00,
      "feeAmount": 10.00,
      "totalAmount": 110.00,
      "status": "SUCCESS",
      "createdAt": "2026-02-05T14:07:53.912852",
      "updatedAt": "2026-02-05T14:07:53.912856"
    }
  }
}
```

**Verification:**
- Payment ID: Added
- Status: Changed from NEW to SUCCESS

**Status: PASS**

---

### 5. GET /orders/{orderId} - Get Order Details

**Request:**
```bash
curl http://localhost:8080/orders/d97b0800-2be9-4a87-a3ff-f48bb6e736d3
```

**Response (200 OK):**
```json
{
  "data": {
    "type": "orders",
    "id": "d97b0800-2be9-4a87-a3ff-f48bb6e736d3",
    "attributes": {
      "providerId": "b04ce6ce-2478-43fb-8832-39d8ad10b694",
      "accountNumber": "+639123456789",
      "paymentId": "550e8400-e29b-41d4-a716-446655440002",
      "providerName": "Globe",
      "baseAmount": 100.00,
      "feeAmount": 10.00,
      "totalAmount": 110.00,
      "status": "SUCCESS",
      "createdAt": "2026-02-05T14:07:53.912852",
      "updatedAt": "2026-02-05T14:07:53.935453"
    }
  }
}
```

**Status: PASS**

---

## Unit Test Results

```
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Test Class | Tests | Status |
|------------|-------|--------|
| DisbursementApplicationTests | 1 | PASS |
| ProviderServiceTest | 5 | PASS |
| OrderServiceTest | 6 | PASS |
| ProviderControllerTest | 3 | PASS |
| OrderControllerTest | 4 | PASS |
| **TOTAL** | **19** | **ALL PASS** |

---

## Summary

| API Endpoint | Method | Expected | Actual | Status |
|--------------|--------|----------|--------|--------|
| /providers | POST | 201 | 201 | PASS |
| /providers | GET | 200 | 200 | PASS |
| /orders/load | POST | 201 | 201 | PASS |
| /orders/load/{id} | POST | 200 | 200 | PASS |
| /orders/{id} | GET | 200 | 200 | PASS |

**All 5 API endpoints tested and working correctly.**
**All 19 unit tests passed.**
