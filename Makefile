.PHONY: help db-up db-down build test run fe-install fe-dev

help:
	@echo "=== SLA Attribution Engine Commands (Spring Boot + PostgreSQL + LLM API + React) ==="
	@echo "make db-up      : Start lightweight PostgreSQL 16 container"
	@echo "make db-down    : Stop containers"
	@echo "make run        : Run Spring Boot Backend locally (port 8080)"
	@echo "make test       : Run Backend Unit & Integration Tests"
	@echo "make fe-install : Install React Frontend dependencies"
	@echo "make fe-dev     : Run React Frontend dev server (port 5173)"

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

run:
	mvn spring-boot:run -Dspring-boot.run.profiles=local

test:
	mvn test

build:
	mvn clean package -DskipTests

fe-install:
	cd frontend && npm install

fe-dev:
	cd frontend && npm run dev
