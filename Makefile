.PHONY: test coverage docker-up docker-down clean

# Run tests
test:
	./mvnw clean test

# Run tests and open JaCoCo coverage report
coverage: test
	@echo "Opening coverage report..."
	@open target/site/jacoco/index.html || xdg-open target/site/jacoco/index.html || echo "Open the file manually: target/site/jacoco/index.html"

# Build and run with Docker
docker-up:
	./mvnw clean package -DskipTests
	docker compose up --build -d

# Stop Docker containers
docker-down:
	docker compose down

# Clean the project
clean-install:
	./mvnw clean install -DskipTests
