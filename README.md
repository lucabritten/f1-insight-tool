# F1 Insight CLI Tool 🏎️

A command-line tool to explore Formula 1 data such as drivers, weather, sessions and meetings. The tool queries the public OpenF1 API and can cache/query data locally via SQLite and jOOQ. Data is supported from season 2023 onwards.

---

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage](#usage)
  - [Global Help](#global-help)
  - [driver-info](#driver-info)
  - [weather-info](#weather-info)
  - [fastest-lap](#fastest-lap)
- [Build & Run](#build--run)
- [Configuration](#configuration)
- [Data Sources](#data-sources)
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
- Local persistence via SQLite, with jOOQ for type-safe data access
- Simple, consistent CLI UX with Picocli
- Tested with JUnit 5 and AssertJ

## Architecture
High-level components:
- `F1CLI` – root command that wires subcommands
- `DriverCommand`, `WeatherCommand` and `LapCommand` – CLI entry points (Picocli)
- `DriverService`, `WeatherService`, ... – domain/business logic
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
```

## Quick Start
Run a driver info lookup (uses Maven Exec Plugin):
```bash
mvn -q exec:java -Dexec.args="driver-info --firstName Max --lastName Verstappen --year 2024"
```

Or build a shaded JAR and run it:
```bash
mvn clean package
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar driver-info --firstName Max --lastName Verstappen --year 2024
```

## Usage
### Global Help
Show global help and the list of subcommands:
```bash
mvn -q exec:java -Dexec.args="--help"
# or, if running the JAR
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar --help
```

### driver-info
Prints driver information for a given season.

Options:
- `--firstName, -fn` (required): driver first name
- `--lastName, -ln` (required): driver last name
- `--year, -y` (optional, default: `2024`): season year (supported: 2023+)

Examples:
```bash
mvn -q exec:java -Dexec.args="driver-info --firstName Lewis --lastName Hamilton --year 2023"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar driver-info -fn Charles -ln Leclerc -y 2024
```

### weather-info
Prints averaged weather information for a given event location, year, and session.

Options:
- `--location, -l` (required): event location (e.g., Austin)
- `--year, -y` (optional, default: `2024`)
- `--sessionName, -sn` (required): session type (e.g., `Race`, `Qualifying`, `Practice`)

Examples:
```bash
mvn -q exec:java -Dexec.args="weather-info --location Austin --year 2024 --sessionName Race"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar weather-info -l Austin -y 2024 -sn Qualifying
```

### fastest-lap
Prints the fastest lap for a given event location, year, and session. Optionally filter by driver number.

Options:
- `--location, -l` (required): event location (e.g., Austin)
- `--year, -y` (optional, default: `2024`)
- `--sessionName, -sn` (required): session type (e.g., `Race`, `Qualifying`, `Practice`)
- `--driverNumber, -dn` (optional): driver number to filter by

Examples:
```bash
mvn -q exec:java -Dexec.args="fastest-lap --location Austin --year 2024 --sessionName Race"
java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar fastest-lap -l Austin -y 2024 -sn Race -dn 44
```

## Build & Run
- Build with tests: `mvn clean verify`
- Build without tests: `mvn -DskipTests clean package`
- Run via Maven Exec Plugin: `mvn -q exec:java -Dexec.args="<args>"`
- Run shaded JAR: `java -jar target/f1-insight-tool-1.0-SNAPSHOT.jar <args>`

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
