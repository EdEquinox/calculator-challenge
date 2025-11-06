# Calculator Application

This repository is a multi-module Java application that implements a calculator service and a REST API. It uses Spring Boot and Kafka for messaging between services and follows a request/response pattern over Kafka for operation processing.

Key technologies
- Java (Gradle)
- Spring Boot 3 (Spring Web, Spring Kafka)
- React (Vite) and Nginx
- Kafka (topics used for requests and results)
- Logback for logging
- Docker / Docker Compose for containerized execution

Modules
- `api/` — shared models and utilities (DTOs, filters context)
- `calculator/` — calculator service: consumes operation requests, computes results and publishes replies
- `rest/` — REST API: accepts HTTP requests, publishes operation requests to Kafka and waits for replies
- `frontend\` - Lightweight frontend (HTML + JS) packaged with Vite and served by Nginx

Architecture (short)
- Clients call the `rest` service (HTTP GET endpoints) to perform operations.
- The `rest` service publishes an `OperationRequest` message to the `operation-requests` topic and waits (short timeout) for an `OperationResult` on the `operation-results` topic.
- The `calculator` service consumes `operation-requests`, computes the result and publishes an `OperationResult` keyed by the request id.
- Correlation is performed using a request id propagated as the Kafka message key and as the `X-Request-ID` HTTP response header. A servlet filter populates the request id and stores it in MDC via a shared `FiltersContext` helper.

Kafka topics
- `operation-requests` — request messages published by `rest` and consumed by `calculator`.
- `operation-results` — reply messages published by `calculator` and consumed by `rest`.

Ports (defaults)
- `rest`: `8080`
- `calculator`: `8081`

Build

From the repository root (recommended using the Gradle wrapper):

PowerShell (Windows):
```powershell
.\gradlew.bat clean build
```

macOS / Linux:
```bash
./gradlew clean build
```

This compiles all modules and produces JARs under `*/build/libs/`.

Run locally (development)

- Run `calculator` in development mode:
```powershell
.\gradlew.bat :calculator:bootRun
```
- Run `rest` in development mode:
```powershell
.\gradlew.bat :rest:bootRun
```

Run with Docker Compose

The repository includes `Dockerfile`s and a `docker-compose.yml` to run the services together with an embedded Kafka container (if configured in compose).

Build and start containers:
```powershell
docker-compose up --build -d
```

Check logs:
```powershell
docker logs -f <service-name>
```
Frontend
--------

This repository includes a lightweight frontend (HTML + JS) packaged with Vite and served by Nginx in the `frontend-web` container.

- Main files:
  - `frontend/index.html` — static landing page (button-driven calculator UI).
  - `frontend/src/main.js` — ES module script that powers the UI and calls the backend endpoints.
  - `frontend/Dockerfile` — two-stage build: `node` to produce the `dist`, and `nginx` to serve static assets.
  - `frontend/nginx/nginx.conf` — Nginx configuration that serves `index.html` and proxies API calls to the REST service.

Build and run (via Docker Compose)

- Rebuild the frontend and restart the service with Docker Compose:
```powershell
docker-compose up --build -d frontend-web
docker-compose logs -f frontend-web
```

- The `frontend-web` container listens on port `80` and is mapped to port `3000` on the host (see `docker-compose.yml`).

How the Nginx proxy works

- Nginx serves the static assets and proxies the API routes `/add`, `/subtract`, `/multiply`, and `/divide` to `http://rest-api:8080`.
- This allows `http://localhost:3000` to call backend endpoints without CORS problems because Nginx forwards the requests internally to the `rest-api` service.

Testing the frontend and proxy

- Test via the frontend proxy (recommended):
```powershell
# call through the frontend nginx which should forward to the REST service
curl -v "http://localhost:3000/subtract?operand1=5&operand2=6"
```
- Test the REST service directly (for comparison):
```powershell
curl -v "http://localhost:8080/subtract?operand1=5&operand2=6"
```

If the response from `localhost:3000` is HTML (for example `index.html`) instead of JSON, check:
- That `nginx.conf` includes `try_files $uri $uri/ /index.html;` so the SPA is served correctly.
- That API locations are declared like `location ~ ^/(add|subtract|multiply|divide)$ { proxy_pass http://rest-api:8080; }` so `GET /subtract?...` requests are routed to the backend.

Common errors and fixes

- `Vite` / `npm install` fails: check `frontend/package.json` and remove unusual overrides; using a stable `vite` version (e.g. `^4.4.0`) normally fixes package resolution issues.
- `Failed to resolve /main.js from /index.html`: ensure `index.html` references the correct script entry. This project uses `type="module" src="/src/main.js"` and provides `frontend/src/main.js`.
- If the browser shows CORS or blocked requests, ensure you are accessing `http://localhost:3000` (frontend) rather than calling `http://localhost:8080` directly while using the frontend served by Nginx.

Notes

- The simple UI in `frontend/src/main.js` sends parameters `operand1` and `operand2` (for example: `/subtract?operand1=5&operand2=6`) and displays the `X-Request-ID` returned by the backend.
- If you prefer to use a dev server (`npm run dev`) instead of the container, add a proxy in your Vite/CRA config to forward `/add` etc. to `http://localhost:8080`, or call the backend directly from JavaScript. In production the Nginx proxy handles forwarding.

Logs
-----

This project writes logs to both the container console and to file (configured by `logback-spring.xml` in each module).

- Container console (recommended for quick checks):
	- `docker logs -f rest-api` — follow the REST service logs
	- `docker logs -f calculator-app` — follow the calculator service logs

- Log files on the host (mounted by Docker Compose):
	- `./logs/rest/rest.log` — REST service log file
	- `./logs/calculator/calculator.log` — Calculator service log file

	You can tail them on the host:
	```powershell
	Get-Content -Path .\logs\rest\rest.log -Wait -Tail 50    # PowerShell
	tail -f ./logs/rest/rest.log                               # Bash
	```

- Inspect logs inside a running container:
	```powershell
	docker exec -it rest-api tail -f /app/logs/rest.log
	docker exec -it calculator-app tail -f /app/logs/calculator.log
	```

- Log configuration files (edit if you need custom locations or patterns):
	- `rest/src/main/resources/logback-spring.xml`
	- `calculator/src/main/resources/logback-spring.xml`

- Enable debug logging temporarily:
	- Local run: pass the Spring property at startup:
		```powershell
		java -jar rest/build/libs/rest.jar --logging.level.root=DEBUG
		```
	- Docker: set the property as JVM/application arg or via `SPRING_APPLICATION_JSON` in `docker-compose.yml` for the service:
		```yaml
		environment:
			SPRING_APPLICATION_JSON: '{"logging":{"level":{"root":"DEBUG"}}}'
		```

Keep in mind that file logging writes to `/app/logs` inside the container; Docker Compose binds those folders to `./logs/<service>` on the host (see `docker-compose.yml`).

Configuration notes (Kafka JSON deserialization)

Messages may contain a type id header that references older package names. To avoid deserialization ClassNotFoundExceptions (when the header references a legacy package), the consumer configuration has been set to ignore the header and use a default type from the `api` models. These properties are set in each module's `application.properties`:

```
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.use.type.headers=false
spring.kafka.consumer.properties.spring.json.value.default.type=pt.edequinox.api.models.OperationResult
```

Adjust `spring.kafka.consumer.properties.spring.json.value.default.type` per module (`OperationRequest` for calculator consumer, `OperationResult` for the rest consumer) if you change package names.

Endpoints
- GET `/add?operand1=<n>&operand2=<m>` — addition
- GET `/subtract?operand1=<n>&operand2=<m>` — subtraction
- GET `/multiply?operand1=<n>&operand2=<m>` — multiplication
- GET `/divide?operand1=<n>&operand2=<m>` — division

Example request

```bash
curl -v "http://localhost:8080/add?operand1=2&operand2=3"
```

Successful response (JSON body contains the operation result and the service will set header `X-Request-ID`):

```json
{ "requestId": "<uuid>", "operationType": "ADDITION", "result": 5, "error": null }
```

Troubleshooting
- If you see a ClassNotFoundException for `pt.edequinox.models.OperationResult` or similar on startup, check the Kafka deserializer settings described above.
- If `X-Request-ID` is missing or null in responses, ensure the servlet filter in the `rest` module is registered and running (it populates the request id).
- For build issues run Gradle with `--stacktrace` and inspect the module-specific build output.




Tests

Run tests with the Gradle wrapper:

```powershell
.\gradlew.bat test
```

