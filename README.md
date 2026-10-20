# Customer Churn Monitor

A Spring Boot REST API for managing client accounts and their employees, recording employee login activity, calculating client churn indicators, and generating Excel churn reports.

## Features

- Create client accounts and add employees.
- Record employee login activity per client.
- Maintain weekly login history and calculate 2-, 4-, and 12-week churn rates.
- Generate an `.xlsx` client churn report.
- Run login-history, churn-calculation, and report-generation jobs using Spring scheduling.

## Technology

- Java 17
- Spring Boot 3.5.7
- Spring Web and Spring Data JPA
- MySQL
- Apache POI for Excel reports
- Maven

## Prerequisites

- JDK 17 or later
- Maven
- A running MySQL server

Create a MySQL database named `customerchurn` and ensure the database tables match the JPA entities in `src/main/java/com/vantagesystem/custchurn/domain`. Configure the database connection in `src/main/resources/application.properties` before starting the application. Do not commit database credentials to source control.

The application is configured to listen on port `7056` by default.

## Run

From the project root:

```bash
mvn spring-boot:run
```

To build and run tests:

```bash
mvn test
mvn package
```

## REST API

Base URL: `http://localhost:7056`

The request DTOs use Jakarta Bean Validation. Required values are indicated below. Date-time values should use ISO-8601 format, for example `2026-10-20T15:30:00`.

### Create a client account

`POST /customerchurn/addAccount`

```json
{
  "name": "Example Client",
  "licenseStartDateTime": "2026-10-20T00:00:00",
  "licenseEndDateTime": "2027-10-19T23:59:59",
  "clientAddress": "100 Main Street",
  "phoneNumber": "555-0100"
}
```

`name`, `licenseStartDateTime`, and `licenseEndDateTime` are required. The response includes the generated `clientIdentifier` and a result status.

### Add an employee

`POST /customerchurn/addEmployee`

```json
{
  "clientIdentifier": "C123456",
  "employeeId": "employee-001",
  "lastLoginDateTime": "2026-10-20T15:30:00"
}
```

All three fields are required. The response contains a result status and, when available, a result message.

### Record a login

`POST /customerchurn/updateLoginCount`

```json
{
  "clientIdentifier": "C123456",
  "employeeId": "employee-001",
  "loginDateTime": "2026-10-20T15:30:00"
}
```

All fields are required. This operation is processed asynchronously and returns `202 Accepted`.

### Calculate churn percentages

`POST /churnreport/calculateChurnPerc`

Recalculates and stores churn percentages using the clients' recorded weekly login history. Returns `202 Accepted`.

### Generate a churn report

`POST /churnreport/generateChurnReport`

Creates an Excel report with client details, churn rates, and a `Need Action` indicator. The report is written to:

```text
src/main/resources/ClientChurnReport_YYYYMMDD.xlsx
```

The report-generation operation currently returns `202 Accepted`; there is no API endpoint for downloading the generated file.

## Scheduled processing

The application enables Spring scheduling. Scheduled tasks update weekly login history, calculate churn values, and generate the report. Their cron expressions are defined in `ProcessChurnValueScheduler`.

## Project layout

```text
src/main/java/com/vantagesystem/custchurn/
  controller/   REST endpoints
  domain/       JPA entities
  dto/          Request and response types
  job/          Scheduled processing
  repository/   Spring Data JPA repositories
  service/      Application and churn logic
src/main/resources/
  application.properties
  ClientChurnReport_*.xlsx
```
