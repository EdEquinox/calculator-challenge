# Calculator Challenge

A multi-module Java project (Gradle) containing a calculator core and a REST service. This README explains the repository layout, prerequisites, how to build the project locally, run the services (locally or via Docker), run tests, and common troubleshooting notes.

## Project Overview

- **Purpose**: Simple interview coding challenge implementing a calculator service and a REST API to exercise Java/Gradle build and basic microservice conventions.
- **Modules**: The repository contains multiple modules and a top-level project:
  - `calculator/` - calculator core, domain and service logic
  - `rest/` - REST API that depends on calculator logic
  - root project - aggregator and shared configuration

## Repository Layout

Top-level files and folders of interest:

- `build.gradle`, `settings.gradle` - root Gradle configuration
- `gradlew`, `gradlew.bat` - Gradle wrapper (use these to build)
- `calculator/` - calculator module (source, resources, Dockerfile)
- `rest/` - REST module (controllers, models, Dockerfile)
- `docker-compose.yml` - compose file to run services together
- `src/`, `test/` - additional top-level sources and tests

## Prerequisites

- Java JDK 11 or newer (JDK 17 recommended).
- Docker & Docker Compose (optional, for container runs).
- Git (optional, for version control).

Notes:
- The project uses the included Gradle wrapper, so you do not need a separate Gradle installation.

## Build (all modules)

From the repository root, on Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

On macOS/Linux (or WSL):

```bash
./gradlew clean build
```

This will compile all modules, run unit tests, and produce JARs in each module's `build/libs/` directory.

## Run (development)

You can run modules directly with Gradle (useful for development):

- Run the `calculator` module (if it exposes a runnable Spring Boot app):

```powershell
.\gradlew.bat :calculator:bootRun
```

- Run the `rest` module:

```powershell
.\gradlew.bat :rest:bootRun
```

Alternatively, run the built JAR files produced after `build`:

```powershell
java -jar calculator/build/libs/calculator-<version>.jar
java -jar rest/build/libs/rest-<version>.jar
```

Replace `<version>` with the actual artifact version found in the `build/libs` folder (e.g. `0.1.0-SNAPSHOT` or similar).

## Run with Docker / Docker Compose

If you prefer containers, the repository contains `Dockerfile` entries for modules and a `docker-compose.yml` at the root.

Build images and run via Docker Compose:

```powershell
docker-compose up --build
```

This command will build images for modules that have `Dockerfile`s and start containers according to `docker-compose.yml`.

You can also build images manually per module:

```powershell
docker build -t calculator:local ./calculator
docker build -t rest:local ./rest
```

Then run containers with `docker run` or add them to `docker-compose.yml` as needed.

## Tests

Run unit tests with the Gradle wrapper:

```powershell
.\gradlew.bat test
```

Test reports are generated under each module's `build/reports/tests/` directory.

## Configuration & Environment

- Module-specific `application.properties` files are located under `*/src/main/resources` or `bin/default` in each module.
- If a service fails to start due to port conflicts or missing configuration, check the module `application.properties` and adjust environment variables or port settings.

Common environment variables you might set before running:

```powershell
# Example (PowerShell):
$env:SPRING_PROFILES_ACTIVE='local'
$env:SERVER_PORT='8081'
```

Or pass system properties to the JVM when starting the JAR:

```powershell
java -jar rest/build/libs/rest-<version>.jar --server.port=8081
```

## Logging & Troubleshooting

- Application logs appear in console output by default. Check `logs/` or `build/` folders if logging is redirected by the app.
- If build fails, run Gradle with `--stacktrace` for more details:

```powershell
.\gradlew.bat build --stacktrace
```

- Common fixes:
  - Ensure correct Java version is on `PATH` and `JAVA_HOME` is set.
  - Use the Gradle wrapper (`gradlew`/`gradlew.bat`) to avoid Gradle version mismatches.
  - Check for port conflicts when starting multiple services.


## License

This repository does not include an explicit license file. If you plan to reuse or redistribute the code, add an appropriate `LICENSE` file and document usage rights.
