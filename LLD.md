# Customer Churn Monitor - Low-Level Design

## 1. Document purpose

This document describes the current implementation of Customer Churn Monitor: its runtime components, HTTP interfaces, persistence model, scheduled processing, churn calculation, report generation, and operational configuration. It is based on the source code and configuration in this repository; it does not imply behavior that is not currently implemented.

## 2. Application overview

The application is a Java 17 / Spring Boot 3.5.7 service. It exposes REST endpoints for client and employee setup, employee login-count updates, churn calculation, and report generation. Client and employee data are stored in MySQL through Spring Data JPA. Churn history and rates are maintained by service logic, and the report is generated as an Excel `.xlsx` file using Apache POI.

### 2.1 Runtime architecture

```text
REST client
    |
    v
Spring MVC controllers
    |
    v
Application services ------------------ Scheduled jobs
    |                                        |
    v                                        v
Spring Data JPA repositories <---------------+
    |
    v
MySQL (client_details, employee_details)

CustomerChurnService
    |
    v
Apache POI -> ClientChurnReport_yyyyMMdd.xlsx

Application and business logs -> console and logs/customer-churn-monitor.log
```

### 2.2 Main components

| Component | Responsibility |
|---|---|
| `CustomerChurnMonitorApplication` | Spring Boot entry point; enables asynchronous methods and scheduled tasks. |
| `CustomerChurnController` | Client creation, employee creation, and login-count update endpoints under `/customerchurn`. |
| `ChurnReportController` | Churn calculation and report-generation endpoints under `/churnreport`. |
| `AccountSetupService` | Maps a client setup request to a client entity and persists it. |
| `AddEmployeeService` | Maps an employee setup request to an employee entity and persists it. |
| `LoginCountService` | Asynchronously increments an employee's login count and updates the last-login timestamp. |
| `CustomerChurnService` | Aggregates weekly login counts, calculates churn rates, and generates the Excel report. |
| `ProcessChurnValueScheduler` | Invokes the weekly-history, churn-calculation, and report-generation operations on cron schedules. |
| `ClientDetailsRepository` | Spring Data JPA access to client records. |
| `EmployeeDetailsRepository` | Spring Data JPA access to employee records and login-count update/aggregation queries. |

Controllers and services use constructor injection. The application uses Log4j 2 through Spring Boot's `spring-boot-starter-log4j2`.

## 3. HTTP API design

All operations currently use HTTP `POST`. The request DTOs use Jakarta Bean Validation and are annotated with `@Valid` at the controller boundary.

### 3.1 Client and employee endpoints

| Method and path | Request | Behavior | Response |
|---|---|---|---|
| `POST /customerchurn/addAccount` | `ClientSetupRequest`: `name` (not blank), `licenseStartDateTime` (not null), `licenseEndDateTime` (not null), optional `clientAddress`, optional `phoneNumber` | Generates a client identifier, maps the request to `ClientDetails`, and saves it. | `ClientSetupResponse` with `name`, `clientIdentifier`, `result`, and optional `resultMsg`. The controller returns the DTO with the default success status. |
| `POST /customerchurn/addEmployee` | `AddEmployeeRequest`: `clientIdentifier` and `employeeId` (not blank), `lastLoginDateTime` (not null) | Maps the request to `EmployeeDetails` and saves it. | `AddEmployeeResponse` with `result` and `resultMsg`. The controller returns the DTO with the default success status; persistence failure is represented in the response body. |
| `POST /customerchurn/updateLoginCount` | `UpdateLoginCountRequest`: `clientIdentifier` and `employeeId` (not blank), `loginDateTime` (not null) | Starts the `@Async` service method and returns without waiting for the database update to finish. | HTTP `202 Accepted`, empty body. This indicates acceptance, not successful completion of the asynchronous update. |

### 3.2 Churn/report endpoints

| Method and path | Behavior | Response |
|---|---|---|
| `POST /churnreport/calculateChurnPerc` | Runs churn calculations for clients with stored login history. | HTTP `202 Accepted`, empty body. The service call itself is synchronous in the request thread. |
| `POST /churnreport/generateChurnReport` | Generates the Excel report from current client records. | HTTP `202 Accepted`, empty body. The service call itself is synchronous in the request thread. |

The two churn-report methods do not require a request body. No authentication or authorization configuration is present in the inspected application code.

## 4. Data model and persistence

Spring Data JPA maps the following entities to MySQL tables. Entity lifecycle callbacks assign creation and last-update timestamps. The source contains no explicit schema migration scripts or configured JPA DDL mode; database schema provisioning is therefore external to these classes.

### 4.1 `client_details`

| Column | Java property | Notes |
|---|---|---|
| `id` | `id` | `Long`, generated identity primary key. |
| `client_identifier` | `clientIdentifier` | Required, unique, maximum length 7. |
| `name` | `name` | Client name. |
| `address` | `address` | Client address. |
| `phone_number` | `phoneNumber` | Client phone number. |
| `create_date` | `createDate` | Required, set on insert, not updated afterward. |
| `last_update_date` | `lastUpdateDate` | Set on insert and update. |
| `license_start_datetime` | `licenseStartDateTime` | License start date/time. |
| `license_end_datetime` | `licenseEndDateTime` | License end date/time. |
| `churn_rate_2_weeks` | `churnRate2Weeks` | Decimal precision 5, scale 2. |
| `churn_rate_4_weeks` | `churnRate4Weeks` | Decimal precision 5, scale 2. |
| `churn_rate_12_weeks` | `churnRate12Weeks` | Decimal precision 5, scale 2. |
| `login_weekly_history` | `loginWeeklyHistory` | Weekly totals stored as a comma-separated numeric list in brackets, e.g. `[10,12,8]`. |

### 4.2 `employee_details`

| Column | Java property | Notes |
|---|---|---|
| `id` | `id` | `Long`, generated identity primary key. |
| `client_identifier` | `clientIdentifier` | Required; associates the employee with a client by identifier. |
| `employee_id` | `employeeId` | Required employee identifier. |
| `create_date` | `createDate` | Required, set on insert. |
| `last_update_date` | `lastUpdateDate` | Set on insert and update. |
| `login_count_per_day` | `weeklyLoginCount` | Integer counter incremented by login updates and reset by the scheduled aggregation. |
| `last_login_datetime` | `lastLoginDateTime` | Updated when a login-count update is applied. |

The association between client and employee is represented by the shared `clientIdentifier` value; no JPA relationship or database foreign-key mapping is declared in the entity classes.

### 4.3 Repository operations

- `ClientDetailsRepository` provides standard `JpaRepository` operations.
- `EmployeeDetailsRepository.updateLoginCount` updates the last-login time and increments the count using `COALESCE(count, 0) + 1` for the matching client and employee identifiers. It returns the number of rows updated.
- `EmployeeDetailsRepository.getWeeklyLoginCount` sums employee counts for a client and returns zero when the sum is null.
- `EmployeeDetailsRepository.resetWeeklyLoginCount` sets employee counts to zero for the specified client.

## 5. Business workflows

### 5.1 Client account creation

1. Spring MVC validates the required request fields.
2. `AccountSetupService` creates a `ClientDetails` instance and copies name, license dates, address, and phone number.
3. The service generates an identifier using `"C" + (current epoch seconds)`.
4. The entity is saved through `ClientDetailsRepository`.
5. The service returns a success response on save, or a failure result/message if an exception is caught.

### 5.2 Employee setup

1. Spring MVC validates the required request fields.
2. `AddEmployeeService` maps client identifier, employee ID, and last-login timestamp to an `EmployeeDetails` entity.
3. The entity is saved through `EmployeeDetailsRepository`.
4. The service returns `SUCCESS` after save, or `Failure` and a failure message when persistence throws an exception.

### 5.3 Login-count update

1. The controller validates the request and invokes `LoginCountService.updateLoginCount`.
2. Because the method is annotated `@Async` and async support is enabled, work is submitted asynchronously.
3. The repository updates the last-login timestamp and increments the matching employee's count.
4. If zero rows are updated, the service logs a warning and records the resulting failure in its error log. Repository exceptions are also logged.
5. The HTTP request returns `202 Accepted` without a result body; the caller does not receive the eventual database outcome through this API.

### 5.4 Weekly login aggregation

For each client returned by `ClientDetailsRepository.findAll()`:

1. Sum that client's employee login counters.
2. Append the sum to `loginWeeklyHistory` (`[count]` for the first value, otherwise append a comma-separated value before the closing bracket).
3. Save the client.
4. Reset the employee counters for that client to zero.

The method is annotated `@Transactional`. The aggregation and counter reset are intended to keep a historical weekly series while restarting the active counters.

### 5.5 Churn rate calculation

For each client, the service parses the stored bracketed login-history string as a list of integers. The calculation examines the most recent history values in a window, splits the window into equal older and newer halves, computes each half's average, and returns percentage change:

```text
percentage change = ((newer average - older average) / older average) * 100
```

| Stored rate | Window | Required history length in current code |
|---|---:|---:|
| `churnRate2Weeks` | Most recent 4 values; compare 2 values against 2 values | At least 4 |
| `churnRate4Weeks` | Most recent 8 values; compare 4 values against 4 values | At least 8 |
| `churnRate12Weeks` | Most recent 24 values; compare 12 values against 12 values | More than 23 (at least 24) |

The service only updates a rate when the computed change is non-zero. A zero change therefore does not clear a previously stored value.

### 5.6 Excel report generation

1. Create an `.xlsx` workbook with a `Client Churn Report` sheet.
2. Add columns for sequence number, client name and identifier, license dates, three churn rates, and `Need Action`.
3. Populate one row per client. Missing churn rates are treated as zero for the action decision.
4. Set `Need Action` to `YES` if the 2-week rate is at most `-20`, the 4-week rate at most `-35`, or the 12-week rate at most `-40`; otherwise set it to `NO`.
5. Write the workbook to `src/main/resources/ClientChurnReport_yyyyMMdd.xlsx`, with the date based on `LocalDate.now()`.

Generation and write errors are logged. The method does not return the file to the HTTP caller.

## 6. Scheduled processing

Scheduling is enabled on the Spring Boot application. `ProcessChurnValueScheduler` declares:

| Operation | Cron expression | Actual timing implied by the expression |
|---|---|---|
| Weekly login-history update | `0 12 19 * * *` | Every day at 19:12:00. Despite the method/comment description, this expression is daily, not weekly. |
| Churn calculation | `* 26 15 * * *` | Every second during minute 26 of hour 15, every day. |
| Report generation | `* 44 16 * * *` | Every second during minute 44 of hour 16, every day. |

No explicit scheduler timezone is specified. Spring therefore evaluates these schedules using the scheduler's configured/default timezone. The cron expressions should be reviewed before production use, particularly because the second and third expressions run once per second for a full minute.

## 7. Configuration and dependencies

Configuration is in `src/main/resources/application.properties`:

| Setting | Current value/purpose |
|---|---|
| Server port | `7056` |
| Application name | `CustomerChurnMonitor` |
| Log file | `logs/customer-churn-monitor.log` relative to the process working directory |
| Root and application log levels | `INFO` |
| Database | MySQL database `customerchurn` on `localhost:3306`; credentials are supplied through application configuration and are intentionally not reproduced here. |
| Hibernate JDBC timezone | `America/New_York` |

The Maven build declares Spring Boot web, data JPA, validation, Log4j 2, MySQL Connector/J, Apache POI, Jackson, Lombok, DevTools, and Spring Boot test dependencies. Spring Boot dependency management supplies versions unless explicitly specified in the POM.

Log4j 2 is configured via Spring Boot's logging properties. The expected destinations are console output and the configured log file. No custom Log4j XML/JSON configuration, retention policy, or rotation settings are present in the source inspected for this design.

## 8. Error handling and observability

- Client and employee persistence failures are logged by their services and returned as result fields in DTOs.
- Login-count update failures are logged from the asynchronous service; the endpoint has already returned `202 Accepted`.
- Report-generation and report-write failures are logged; the report endpoints still return `202 Accepted` after the service returns.
- Controller entry, major service operations, scheduled operation starts/completions, and report generation outcomes are logged.
- No request body or employee identifier is intentionally included in the controller logs.
- No global exception handler or standardized error response mapping is defined in the inspected source.

## 9. Implementation constraints and follow-up considerations

These are observations about the current implementation, not behavior added by this document:

1. **Client identifier length:** generated identifiers use a `C` prefix followed by epoch seconds, while `client_identifier` has a declared maximum length of 7. The generated value may exceed that limit and cause persistence failure.
2. **Schedule cadence:** the weekly aggregation schedule is currently daily, and the churn/report cron expressions execute every second during their selected minute. Confirm the desired schedule and set an explicit timezone if required.
3. **Churn division by zero:** if the older-period average is zero, the percentage formula divides by zero. The result may not be a usable finite percentage.
4. **History format:** history is stored as a string and parsed directly as comma-separated integers. Invalid or manually edited history can interrupt churn calculation.
5. **Report path:** report output is written relative to the working directory into the source resources directory. This may not be writable or present in a packaged deployment; the file is not exposed as a download endpoint.
6. **Async outcome visibility:** login updates run asynchronously but the API provides no status resource or completion response to the caller.
7. **Data integrity:** the entity model does not declare a client-to-employee relationship or foreign key. Database constraints and referential integrity must be managed separately if required.
8. **Database secrets:** credentials should be supplied through environment-specific external configuration or a secret manager rather than committed application properties.

## 10. Source map

- Application entry point: `src/main/java/com/vantagesystem/custchurn/CustomerChurnMonitorApplication.java`
- HTTP controllers: `src/main/java/com/vantagesystem/custchurn/controller/`
- DTOs: `src/main/java/com/vantagesystem/custchurn/dto/`
- JPA entities: `src/main/java/com/vantagesystem/custchurn/domain/`
- Repositories: `src/main/java/com/vantagesystem/custchurn/repository/`
- Business services: `src/main/java/com/vantagesystem/custchurn/service/`
- Scheduled jobs: `src/main/java/com/vantagesystem/custchurn/job/ProcessChurnValueScheduler.java`
- Runtime configuration: `src/main/resources/application.properties`
