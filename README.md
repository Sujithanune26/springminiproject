# Banking System Simulator (Spring Boot + MongoDB)

**Mini Project** — Banking Services Application (Monolith)

---

## 📌 Overview

This project is a Spring Boot monolithic backend that simulates basic banking operations. It exposes RESTful APIs for account management and transactions (deposit, withdraw, transfer) and persists data in MongoDB. The app follows the Controller → Service → Repository → Model layered architecture, with validation, custom exceptions, global exception handling, and unit tests (JUnit 5 + Mockito).

---

## ✅ Features

* Create, retrieve, update, and delete accounts
* Deposit, withdraw, and transfer funds
* Transaction history per account
* Auto-generated account numbers and transaction IDs
* Input validation (Jakarta Bean Validation)
* Custom exceptions and a centralized `@ControllerAdvice`
* Logging with SLF4J
* Unit tests covering service/controller/exception/model layers

---

## 🛠 Tech Stack

* Java 17+ (or compatible LTS)
* Spring Boot
* Spring Data MongoDB
* Jakarta Bean Validation
* JUnit 5 + Mockito (unit testing)
* Maven
* MongoDB (local or Atlas)

---

## ⚙️ Prerequisites

* Java 17+
* Maven 3.6+
* MongoDB (local or Atlas)
* Git

---

## 🔧 Setup & Run (Local)

1. Clone the repo

```bash
git clone <your-repo-url>
cd banking-system
```

2. Configure MongoDB connection in `src/main/resources/application.properties` (example):

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/bankdb
server.port=8080
```

> If using MongoDB Atlas, set the Atlas connection URI in the same property.

3. Build and run

```bash
mvn clean package
mvn spring-boot:run
```

The server runs on `http://localhost:8080` by default.

---

## 🔐 Validation & Rules (important)

* Account number format: **3 uppercase letters + 4 digits** (e.g., `ASH1234`). If format invalid, service throws `InvalidAccountNumberException` and the global handler returns `400 Bad Request`.
* Amounts must be numeric and at least 1. Negative or zero amounts throw `InvalidAmountException`.
* Withdraw and transfer validate sufficient balance; otherwise `InsufficientBalanceException` is thrown.
* Transfer disallows source == destination.

---

## 🧭 API Endpoints

Base path: `/api/accounts`

| Operation          | Method | Endpoint                                     | Request body                                                                       |   Success status |
| ------------------ | -----: | -------------------------------------------- | ---------------------------------------------------------------------------------- | ---------------: |
| Create account     |   POST | `/api/accounts`                              | `{ "holderName": "John Doe" }`                                                     |    `201 Created` |
| Get account        |    GET | `/api/accounts/{accountNumber}`              | —                                                                                  |         `200 OK` |
| Update holder name |    PUT | `/api/accounts/{accountNumber}`              | `{ "holderName": "New Name" }`                                                     |         `200 OK` |
| Delete account     | DELETE | `/api/accounts/{accountNumber}`              | —                                                                                  | `204 No Content` |
| Deposit            |    PUT | `/api/accounts/{accountNumber}/deposit`      | `{ "amount": 100.0 }`                                                              |         `200 OK` |
| Withdraw           |    PUT | `/api/accounts/{accountNumber}/withdraw`     | `{ "amount": 50.0 }`                                                               |         `200 OK` |
| Transfer           |   POST | `/api/accounts/transfer`                     | `{ "sourceAccount": "SRC1234", "destinationAccount": "DST1234", "amount": 100.0 }` |         `200 OK` |
| Get transactions   |    GET | `/api/accounts/{accountNumber}/transactions` | —                                                                                  |         `200 OK` |

---

## 🔬 DTOs (request bodies)

**CreateAccountRequest**

```json
{ "holderName": "John Doe" }
```

**AmountRequest**

```json
{ "amount": 100.0 }
```

**TransferRequest**

```json
{
  "sourceAccount": "JOH1234",
  "destinationAccount": "ANN5678",
  "amount": 250.0
}
```

---

## 🧾 Sample cURL Requests

Create account:

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"holderName":"John Doe"}'
```

Get account:

```bash
curl http://localhost:8080/api/accounts/JOH1234
```

Deposit:

```bash
curl -X PUT http://localhost:8080/api/accounts/JOH1234/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.0}'
```

Transfer:

```bash
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d '{"sourceAccount":"JOH1234","destinationAccount":"ANN5678","amount":50}'
```

---

## 🗂 Data Models (examples)

**Account document (accounts collection)**

```json
{
  "_id": "...",
  "accountNumber": "JOH2871",
  "holderName": "John Doe",
  "balance": 12000.5,
  "status": "ACTIVE",
  "createdAt": "2025-11-07T09:30:00Z",
  "transactionIds": ["TXN-100001","TXN-100002"]
}
```

**Transaction document (transactions collection)**

```json
{
  "_id": "...",
  "transactionId": "TXN-20251107-001",
  "type": "TRANSFER",
  "amount": 500.0,
  "timestamp": "2025-11-07T09:32:10Z",
  "status": "SUCCESS",
  "sourceAccount": "JOH2871",
  "destinationAccount": "ANN9810"
}
```

---

## ✅ Exception Handling

All custom exceptions are handled by `GlobalExceptionHandler` annotated with `@ControllerAdvice`.

* `AccountNotFoundException` → `404 Not Found`
* `InvalidAmountException`, `InsufficientBalanceException`, `InvalidAccountNumberException` → `400 Bad Request`
* `MethodArgumentNotValidException` (validation errors) → `400 Bad Request` with first validation message

---

## 🧪 Running Tests & Coverage

Run unit tests with Maven:

```bash
mvn test
```

To generate a Jacoco coverage report (if Jacoco plugin configured):

```bash
mvn clean test jacoco:report
```

Open the HTML report:

```
target/site/jacoco/index.html
```

> For the README, include a screenshot of `target/site/jacoco/index.html` showing overall coverage (recommended: 70%+). Add that screenshot under `docs/coverage.png` and reference it in README using `![coverage](docs/coverage.png)`.

---

## 📦 Packaging & Docker (optional)

**Build jar**

```bash
mvn clean package
```

**Dockerfile (example)**

```dockerfile
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## 🧭 Postman

* Create a Postman collection with all endpoints and example bodies.
* Export the collection and include `postman/BankingSystem.postman_collection.json` in repo.

---

## 🔍 Notes & Tips (for graders / future you)

* Controller tests use Mockito to mock `AccountService`. Controller unit tests **should not** test business logic — they ensure controller wiring, request/response status and payload.
* Service tests mock repositories and verify business rules (balance checks, transaction creation, account number validation). These are the critical tests.
* Repository interfaces extend `MongoRepository` — they are not unit-tested with heavy DB interactions. For integration tests, use an embedded MongoDB (or Testcontainers) if needed.
* Keep `IdGenerator` deterministic in tests by mocking randomness or by not relying on exact generated strings.

---

## 📂 Project Structure

```
com.bankingsystem
├── controller
├── service
│   └── impl
├── repository
├── model
├── dto
├── exception
├── util
└── config
```

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch
3. Make changes and add tests
4. Ensure tests pass and coverage stays >= 70%
5. Open a pull request

---

## 📄 License

This project is provided for educational purposes. Add a license (MIT/Apache) as needed.

---

> If you want, I can also:
>
> * Generate a `postman` collection file
> * Create a `docker-compose` for app + MongoDB
> * Produce the certificate-style cover page for submission

--

*README generated for the Banking System Simulator project.*
