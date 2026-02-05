# F1 Insight CLI Tool 🏎️

A command-line tool to explore Formula 1 data such as drivers, weather, sessions, and meetings. The tool queries the public OpenF1 API and can cache/query data locally via SQLite and jOOQ. Data is supported from season 2023 onwards.

---

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Build & Run](#build--run)
- [Usage](#usage)
  - [Global Help](#global-help)
  - [driver](#driver)
  - [weather](#weather)
  - [laps](#laps)
  - [fastest-laps](#fastest-laps)
  - [session-report](#session-report)
- [Exit Codes](#exit-codes)
- [Caching Strategy](#caching-strategy)
- [Configuration](#configuration)
- [Data Sources](#data-sources)
- [Limitations](#limitations)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Acknowledgements](#acknowledgements)
- [Team](#team)

---

## Overview
F1 Insight is a Java CLI application that provides quick insights into Formula 1:
- Look up driver information by name and season
- Inspect averaged weather for a given event (location), year and session type

It is built with a layered architecture and a clean separation between CLI, services, repositories (SQLite + jOOQ), and API clients (OpenF1).

## Features
- Driver lookup by first and last name for a specific year
- Weather aggregation by location, year and session name (e.g., Race, Qualifying)
- Fastest lap lookup by location, year, session, with optional driver filter
- PDF session reports with weather, results, and lap time comparisons graphic
- Local persistence via SQLite, with jOOQ for type-safe data access
- Simple, consistent CLI UX with Picocli
- Tested with JUnit 5 and AssertJ

## Architecture
High-level components:
- `F1CLI` – root command that wires subcommands
- `*Command` – CLI entry points (Picocli)
- `*Service`, ... – domain/business logic
- `Jooq*Repo` – repositories using jOOQ over SQLite
- `*Client` – HTTP clients integrating with OpenF1

Main entry-point: `htwsaar.nordpol.App` (starts Picocli).

## Requirements
- Java 21
- Maven 3.9+

## Installation
```bash
git clone https://github.com/lucabritten/f1-insight-tool.git
cd f1-insight-tool
mvn clean package
```

## Quick Start
Run a driver info lookup (uses Maven Exec Plugin):
```bash
mvn -q exec:java -Dexec.args="driver --first-name Max --last-name Verstappen --year 2024"
```

Or build a shaded JAR and run it:
```bash
mvn clean package
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar driver-info --first-name Max --last-name Verstappen --year 2024
```

## Build & Run
- Build with tests: `mvn clean verify`
- Build without tests: `mvn -DskipTests clean package`
- Run via Maven Exec Plugin: `mvn -q exec:java -Dexec.args="<args>"`
- Run shaded JAR: `java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar <args>`

## Usage
### Global Help
Show global help and the list of subcommands:
```bash
mvn -q exec:java -Dexec.args="--help"
# or, if running the JAR
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar --help
```

### driver
Prints driver information for a given season.

Options:
- `--first-name, -fn` (required): The drivers first name (e.g Lando, Max, ...)
- `--last-name, -ln` (required): The drivers last name (e.g. Norris, Verstappen, ...)
- `--year, -y` (optional, default: `current year`): season year (supported: 2023+)

Examples:
```bash
mvn -q exec:java -Dexec.args="driver --first-name Lewis --last-name Hamilton --year 2023"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar driver -fn Charles -ln Leclerc -y 2024
```
Preview for command `driver --first-name Lewis --last-name Hamilton --year 2023`
<img width="1068" height="148" alt="image" src="https://github.com/user-attachments/assets/c0d6d033-fa59-49af-946c-dfaff1f4177a" />

### weather
Prints averaged weather information for a given event location, year, and session.

Options:
- `--location, -l` (required): event location (e.g., Austin)
- `--year, -y` (optional, default: `current year`)
- `--session-name, -s`(required): session name (e.g., `Race`, PRACTICE1)

Examples:
```bash
mvn -q exec:java -Dexec.args="weather --location Austin --year 2025 --session-name Qualifying"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar weather -l Monza -y 2023 -s Race
```
Preview vor command `weather --location Austin --year 2025 --session-name Qualifying`
<img width="1254" height="364" alt="image" src="https://github.com/user-attachments/assets/7c1b5e79-4724-4544-aed0-14706bb58cc0" />

### laps
Prints all laps a driver has completed in a specified session of a race-weekend.

Options:
- `--location, -l`(required): event location (e.g., Austin)
- `--year, -y' (optional, default: `current year`)
- `--session-name, -s` (required): session name (e.g., `Race`, PRACTICE1)
- `--driver-number, -d` (required): driver number to filter by

Examples:
```bash
mvn -q exec:java -Dexec.args="laps --location Monza --session-name FP1 --driver-number 1"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar laps -l Monza -y 2024 -s RACE -d 44
```
Preview for command `laps --location 'Spa-Francorchamps' --session-name sprint --driver-number 55 -y 2025`
<img width="1486" height="800" alt="image" src="https://github.com/user-attachments/assets/5da49510-5ae1-4306-90eb-68589fc45119" />

### fastest-laps
Prints the fastest lap for a given event location, year, and session. Optionally filter by driver number.

Options:
- `--location, -l` (required): event location (e.g., Austin)
  - `--year, -y` (optional, default: `current year`)
- `--session-name, -s`(required): session name (e.g., `Race`, PRACTICE1)
- `--driver-number, -d` (optional): driver number to filter by
- `--limit, -lim` (optional): limit to the first N laps (default: 1),
  Examples:
```bash
mvn -q exec:java -Dexec.args="fastest-laps -l Austin -y 2025 -s Qualifying"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar fastest-laps -l Austin -s Race -d 81 --limit 3
```
Preview for command `fastest-laps -l Austin -y 2025 -s Qualifying -lim 5`
<img width="1300" height="404" alt="image" src="https://github.com/user-attachments/assets/6b42dd65-b5b7-4cc4-b56f-6def3f9c92a3" />

### session-report
Generates a PDF report for a session including weather, results, and a lap time comparison chart.

Options:
- `--location, -l` (required): event location (e.g., Monza)
- `--year, -y` (optional, default: `current year`)
- `--session-name, -s` (required): session name (e.g., `Race`, PRACTICE1)
- `--limit -lim` (optional): limit to the first N drivers in result order
- `--output, -o` (optional): output path for the PDF (default: `reports/session-report-<location>-<year>-<session>.pdf`)

Examples:
```bash
mvn -q exec:java -Dexec.args="session-report -l 'Las Vegas' -s Qualifying --limit 10"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar session-report -l Monza -y 2024 -s Race
```
Preview of a possible report (related cmd: `session-report -l Silverstone -y 2025 -s race -lim 5`):
<img width="1286" height="1220" alt="image" src="https://github.com/user-attachments/assets/1a2a064a-c339-47c9-9546-eda5b5c36b00" />


## Exit Codes

The CLI uses standard Unix exit codes:

- `0` - successful execution
- `1` - invalid command usage or invalid parameters
- `2` - no data found for the given criteria

## Caching Strategy
To reduce API calls and improve performance, the application caches data locally
using a SQLite database.

- On each request, the local database is queried first
- If no data is found, the OpenF1 API is queried
- Retrieved data is persisted and reused for later requests

## Configuration
- Network: API calls go to the public OpenF1 API; no API key is required.

## Data Sources
- External API: [OpenF1](https://openf1.org/)
- Local DB: `SQLite` database at `./f1data.db` for caching/queries with jOOQ


Project packages of interest:
- CLI: `htwsaar.nordpol.cli`
- Services: `htwsaar.nordpol.service`
- Repositories: `htwsaar.nordpol.repository.*`
- API Clients & DTOs: `htwsaar.nordpol.api.*`

## Limitations
- Data is available from season 2023 onwards only
- The tool relies on the public OpenF1 API and is subject to its rate limits.

## Troubleshooting
- "Class not found" for jOOQ generated types: ensure `mvn clean package` (or at least `mvn generate-sources`) has been run and `f1data.db` exists in the project root.
- SQLite access issues on macOS/Linux: verify file permissions for `f1data.db`.
- No results for a given season: data is supported for 2023+ only.

## Roadmap
- Advanced analytics and comparisons

## Acknowledgements
- Thanks to the OpenF1 community and maintainers for making F1 data publicly accessible.

## Team
Team Nordpol 🧊
- Gina Rose Kessler
- Niklas Bélières
- Jona David Mees
- Luca Britten
