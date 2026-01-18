# F1 Insight CLI Tool🏎️
CLI-Tool to get information about F1 drivers, events and its correlated data.

## Prerequisites
- Java 21
- Maven 3.9+

## Installation
```bash
git clone https://github.com/lucabritten/f1-insight-tool.git
cd f1-insight-tool

mvn clean package
```

## Quick-start (driver-info cmd)
```bash
mvn -q exec:java -Dexec.args="driver-info --firstName Max -lastName Verstappen"
```

## Features
Driver-Command:
required arguments:

 1. firstname (---firstName, -fn)
 2. lastname (--lastName, -ln)

```bash
driver-info --firstName FIRSTNAME --lastName LASTNAME
```
Prints basic driver details such as country-code or current car number to the console

## Components

- **DriverCommand**
	Handles CLI input and output (PicoCLI).
- **DriverService**
	Contains the application logic and decides where the data comes from.
- **DriverRepo (SQLite + jOOQ)**
	Stores and retrieves driver data locally.
- **DriverClient (OpenF1 API)**
	Fetches driver data from external OpenF1 REST API.

## Technology
- Layered Java Application
- SQLite
- Maven
- Unit-Testing
- PicoCLI
- jOOQ

## Team Nordpol 🧊
- Gina Rose Kessler
- Niklas Beliérès
- Jona David Mees
- Luca Britten
