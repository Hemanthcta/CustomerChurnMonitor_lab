# Customer Churn Monitor - Low-Level Design

## 1. Document purpose

This document describes the current implementation of Customer Churn Monitor: its runtime components, HTTP interfaces, persistence model, scheduled processing, churn calculation, report generation, and operational configuration.

## 2. Application overview

The application is a Java 17 / Spring Boot 3.5.7 service. It exposes REST endpoints for client and employee setup, employee login-count updates, churn calculation, and report generation. Client and employee data are stored in MySQL through Spring Data JPA. Churn history and rates are maintained by service logic, and the report is generated as an Excel `.xlsx` file using Apache POI.



Log folder :  logs/customer-churn-monitor.log
```

### 2.2 Main components

CustomerChurnMonitorApplication: Spring Boot entry point; enables asynchronous methods and scheduled tasks.
CustomerChurnController: Client creation, employee creation, and login-count update endpoints under base URI "/customerchurn".
ChurnReportController: Churn calculation and report-generation endpoints under '/churnreport".
AccountSetupService: Maps a client setup request to a client entity and persists it.
AddEmployeeService: Maps an employee setup request to an employee entity and persists it.
LoginCountService: Asynchronously increments an employee's login count and updates the last-login timestamp.
CustomerChurnService: Aggregates weekly login counts, calculates churn rates, and generates the Excel report.
ProcessChurnValueScheduler: Invokes the weekly-history, churn-calculation, and report-generation operations on cron schedules.
ClientDetailsRepository: Spring Data JPA access to client records.
EmployeeDetailsRepository: Spring Data JPA access to employee records and login-count update/aggregation queries.

Controllers and services use constructor injection. The application uses Log4j 2 through Spring Boot's `spring-boot-starter-log4j2`.

## 3. HTTP API design

All operations currently use HTTP `POST`. The request DTOs use Jakarta Bean Validation and are annotated with `@Valid` at the controller boundary.

### 3.1 Client and employee endpoints

POST /customerchurn/addAccount` ClientSetupRequest`: 
Request : `name` (not blank), `licenseStartDateTime` (not null), `licenseEndDateTime` (not null), optional `clientAddress`, optional `phoneNumber` 
Process: Generates a client identifier, maps the request to `ClientDetails`, and saves it. 
ClientSetupResponse: `name`, `clientIdentifier`, `result`, and optional `resultMsg`. The controller returns the DTO with the default success status.

POST /customerchurn/addEmployee` AddEmployeeRequest`: 
Request: `clientIdentifier` and `employeeId` (not blank), `lastLoginDateTime` (not null)
Process: Maps the request to `EmployeeDetails` and saves it. 
AddEmployeeResponse: `result` and `resultMsg`. The controller returns the DTO with the default success status; persistence failure is represented in the response body.

POST /customerchurn/updateLoginCount` UpdateLoginCountRequest`: 
Request: `clientIdentifier` and `employeeId` (not blank), `loginDateTime` (not null)
Process: Starts the `@Async` service method and returns without waiting for the database update to finish.
Response: HTTP `202 Accepted`, empty body. This indicates acceptance, not successful completion of the asynchronous update.

### 3.2 Churn/report endpoints
POST /churnreport/calculateChurnPerc`:
Runs churn calculations for clients with stored login history.
HTTP `202 Accepted`, empty body.

POST /churnreport/generateChurnReport`: 
Generates the Excel report from current client records. 
HTTP `202 Accepted`, empty body.

The two churn-report methods do not require a request body. No authentication or authorization configuration is present in the inspected application code.

## 4. Data model and persistence

### 4.1 `client_details`

Column Details:
id Long
client_identifier | Required, unique, maximum length 7. |
name 
address 
phone_number
create_date
last_update_date
license_start_datetime
license_end_datetime
churn_rate_2_weeks
churn_rate_4_weeks
churn_rate_12_weeks
login_weekly_history

### 4.2 `employee_details`

Column details:
id Long
client_identifier
employee_id
create_date
last_update_date
login_count_per_day
last_login_datetime

The association between client and employee is represented by the shared `clientIdentifier` value.

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


### 5.5 Churn rate calculation
For each client, the service parses the stored bracketed login-history string as a list of integers. The calculation examines the most recent history values in a window, splits the window into equal older and newer halves, computes each half's average, and returns percentage change:

percentage change = ((newer average - older average) / older average) * 100


Stored rate:
churnRate2Weeks` | Most recent 4 values; compare 2 values against 2 values | At least 4 |
churnRate4Weeks` | Most recent 8 values; compare 4 values against 4 values | At least 8 |
churnRate12Weeks` | Most recent 24 values; compare 12 values against 12 values | More than 23 (at least 24) |

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

| Weekly login-history update 0 5 0 * * SUN` | Every Sunday at 0:05:00.|
| Churn calculation * 0 3 * * SUN` | Every Sunday at 3am. |
| Report generation * 0 9 * * MON` | Every Monday at 9am. |

No explicit scheduler timezone is specified. Spring therefore evaluates these schedules using the scheduler's configured/default timezone.



## 9. Implementation constraints and follow-up considerations

Determine the total number of weeks of login history available for the account.
Calculate the window size as one-fourth of the available history.
If the calculated window size is greater than 3 weeks, use 2 weeks as the window size.
If the calculated window size is greater than 7 weeks, use 4 weeks as the window size.
If the calculated window size is greater than 23 weeks, use 12 weeks as the window size.
Verify that the account has at least twice the number of weeks as the selected window size, because the trend calculation requires both a previous period and a recent period of equal length.
Select the most recent window weeks of login data as the recent period.
Select the window weeks immediately preceding the recent period as the previous period.
Calculate the average login count for the recent period.
Calculate the average login count for the previous period.
If the previous period average is zero, return a trend value of zero to avoid division by zero.
Calculate the percentage change by subtracting the previous average from the recent average, dividing the result by the previous average, and multiplying by 100.


## 10. Source map

- Application entry point: `src/main/java/com/vantagesystem/custchurn/CustomerChurnMonitorApplication.java`
- HTTP controllers: `src/main/java/com/vantagesystem/custchurn/controller/`
- DTOs: `src/main/java/com/vantagesystem/custchurn/dto/`
- JPA entities: `src/main/java/com/vantagesystem/custchurn/domain/`
- Repositories: `src/main/java/com/vantagesystem/custchurn/repository/`
- Business services: `src/main/java/com/vantagesystem/custchurn/service/`
- Scheduled jobs: `src/main/java/com/vantagesystem/custchurn/job/ProcessChurnValueScheduler.java`
- Runtime configuration: `src/main/resources/application.properties`



Assumptions:
--> Each valid updateLoginCount request represents one login and increments that employee’s count by one. Irrespective of time gap between the login requests.
--> Churn calculaiton happens only when minimum of 4 weeks data is available.
--> The report marks a client “Need Action” when the 2-week rate is at most -20%, the 4-week rate at most -35%, or the 12-week rate at most -40%. Confirm that these thresholds are intended.
--> At present authentication is not implemented for HTTP endpoints.
--> The scheduler "weeklyLoginHistoryScheduler" should not be executed more than once in a week.

Improvements:
--> Implement authentication for the HTTP endpoints.
--> Improve the error handling and implement retry mechanism where ever required.
--> Add the SQL connection pooling properties.
--> The generated report can be sent over email.
--> Implement the weekly scheduler to send one more report to notify the near expiry clients list.
--> Update the client_details table, to note if the client license is extended.
--> If large set of data is available, AI can be tried to determine the result in the report based on the previous experience with the clients.
