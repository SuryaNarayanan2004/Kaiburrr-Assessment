# Kaiburr Task Manager (Spring Boot 3 + MongoDB)

Java 17 Spring Boot REST API to manage Tasks backed by MongoDB. Includes safe command execution (only `echo ...`) and Task execution history.

## Tech
- Spring Boot 3.3
- Java 17
- MongoDB (dockerized)
- Maven
- JUnit + MockMvc

## Run locally

Prereqs: Java 17, Maven, Docker

1. Start MongoDB via Docker:
```bash
docker compose up -d mongo
```

2. Run the app:
```bash
mvn spring-boot:run
```
App listens on `http://localhost:8080` and uses MongoDB at `mongodb://localhost:27017/kaiburr`.

Alternatively, run the full stack in Docker:
```bash
mvn -q -DskipTests package
docker compose up -d --build
```

## API

- GET `/tasks` → all tasks
- GET `/tasks?id={id}` → single task
- PUT `/tasks` → create/update task (only `echo ...` allowed)
- DELETE `/tasks/{id}` → delete by id
- GET `/tasks/search?name={namePart}` → search by name substring
- PUT `/tasks/{id}/execute` → execute stored command and append `taskExecutions`

### Task JSON
```json
{
  "id": "string",
  "name": "string",
  "owner": "string",
  "command": "echo hello",
  "taskExecutions": [
    { "startTime": "2025-01-01T10:00:00Z", "endTime": "2025-01-01T10:00:01Z", "output": "hello" }
  ]
}
```

## Example curl

Create/update:
```bash
curl -X PUT http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"name":"Demo","owner":"you","command":"echo hi"}'
```

List all:
```bash
curl http://localhost:8080/tasks
```

Get by id:
```bash
curl "http://localhost:8080/tasks?id=<TASK_ID>"
```

Search by name:
```bash
curl "http://localhost:8080/tasks/search?name=emo"
```

Execute:
```bash
curl -X PUT "http://localhost:8080/tasks/<TASK_ID>/execute"
```

Delete:
```bash
curl -X DELETE "http://localhost:8080/tasks/<TASK_ID>"
```

## Testing
```bash
mvn test
```

## Screenshots
- Use Postman or curl outputs of the above requests. Include list, get by id, create, execute, delete.

## Notes
- Command validation is strict by design to prevent RCE: only `echo ...` is permitted.
- Configure MongoDB via env var `MONGODB_URI`.
