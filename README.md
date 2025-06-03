# 📦 ClusteredData Warehouse

This project provides a RESTful service to import and validate currency deal data from CSV files into a PostgreSQL database.

## 🚀 Features

- Import deals from CSV files via HTTP
- Validate deals (ID, currencies, amount, timestamp)
- Save valid entries to the database
- Track and report invalid or duplicate entries
- Unit-tested with JUnit and JaCoCo coverage

## 🛠️ Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker & Docker Compose
- Maven
- JUnit & Mockito
- JaCoCo (code coverage)
- Lombok

## 📂 File Format

Expected CSV Header:

```csv
dealId,fromCurrency,toCurrency,timestamp,amount
```

Example row:

```csv
123,USD,EUR,2024-06-02T12:00:00,150.75
```


## 📥 API Usage

### `POST /api/import`

Upload a CSV file:

```bash
curl -X POST http://localhost:8080/api/import \\
-H "Content-Type: multipart/form-data" \\
-F "file=@test.csv"
```

Response includes:

- `successCount`
- `duplicateCount`
- `invalidCount`
- `errorMessages[]`

## 🧪 Testing

To run tests and view coverage:

```bash
./mvnw clean test
```
Then open:

```bash
target/site/jacoco/index.html
```

## 🐳 Docker Setup

Build and run using Docker Compose:

```bash
docker-compose up --build
```


# Build the project and run with Docker
```
make docker-up
```

# Run tests
```
make test
```

# Generate coverage report
```
make coverage
```


