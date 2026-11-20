# Smart Waste Collection & Recycling Coordination Platform

A beginner-friendly Spring Boot microservices laboratory project. Households and businesses report recyclable waste; an administrator assigns a collector, schedules collection, records the actual amount, and records transfer to a recycling organization.

## Architecture and ports

This project contains exactly five independent Spring Boot applications and one React/Vite frontend.

| Application | Port | Responsibility |
| --- | ---: | --- |
| Eureka Discovery Server | 8761 | Service registry and dashboard |
| API Gateway | 8080 | Single frontend entry point and service-ID routing |
| Waste Reporting Service | 8081 | Waste reports and report lifecycle |
| Collector Management Service | 8082 | Collectors, availability, and assignments |
| Pickup Scheduling Service | 8083 | Pickup schedule, collection, and handover |
| React/Vite frontend | 5173 | Demonstration screens; calls only port 8080 |

The three business services have independent file-based H2 databases under their own `data` directories. They do not share tables. Cross-service information is exchanged with REST and Spring Cloud OpenFeign, using Eureka service names instead of fixed service IPs. This is a small academic prototype; a failed remote call can leave a previously saved local change, so it does not claim distributed transaction guarantees.

## Requirements and startup (Windows, VS Code, PowerShell)

Install a JDK 17, Maven 3.8+, and Node.js 18+ with npm. Open this folder in VS Code. Open five PowerShell terminals at the project root and run one command in each, in this order. Keep the terminals open:

```powershell
cd .\eureka-server; mvn spring-boot:run
```

After Eureka starts, open `http://localhost:8761`. In four other terminals:

```powershell
cd .\waste-reporting-service; mvn spring-boot:run
cd .\collector-management-service; mvn spring-boot:run
cd .\pickup-scheduling-service; mvn spring-boot:run
cd .\api-gateway; mvn spring-boot:run
```

Allow up to a minute for all services to register. The Eureka dashboard should show `WASTE-REPORTING-SERVICE`, `COLLECTOR-MANAGEMENT-SERVICE`, and `PICKUP-SCHEDULING-SERVICE` (the gateway also registers as a client). Then, in another terminal:

```powershell
cd .\frontend
npm install
npm run dev
```

Open the Vite URL printed by npm, normally `http://localhost:5173`. Frontend API requests go to `http://localhost:8080/api/...`, never directly to a business service.

To build a service independently, run `mvn clean package` in its directory. To build the frontend, run `npm run build` in `frontend`.

## H2 console and persistence

Each business app enables its H2 console at `/h2-console` on its own port:

| Service | Console URL | JDBC URL |
| --- | --- | --- |
| Waste | `http://localhost:8081/h2-console` | `jdbc:h2:file:./data/waste_reporting_db` |
| Collector | `http://localhost:8082/h2-console` | `jdbc:h2:file:./data/collector_management_db` |
| Pickup | `http://localhost:8083/h2-console` | `jdbc:h2:file:./data/pickup_scheduling_db` |

Use driver `org.h2.Driver`, username `sa`, and an empty password. In the console, paste the JDBC URL shown for that service and click **Connect**. Expand **Tables** to inspect `WASTE_REPORT`, `COLLECTOR`, `COLLECTOR_ASSIGNMENT`, `PICKUP_SCHEDULE`, or `RECYCLING_HANDOVER`. The relative `./data` path resolves from that service's working directory (run Maven from its project folder); H2 creates the files on first use. `ddl-auto=update` creates or updates the schema while preserving data. Stop the application before copying or deleting its database files.

## API endpoints

Gateway routes strip the `/api` prefix and forward the remaining path to a service discovered by Eureka.

| Gateway endpoint | Method | Purpose |
| --- | --- | --- |
| `/api/waste` | GET, POST | List/create reports |
| `/api/waste/{id}` | GET, PUT, DELETE | Read/update/cancel a pending report |
| `/api/waste/pending` | GET | Reports awaiting assignment |
| `/api/waste/{id}/status` | PATCH | Validated status transition (body `{"status":"ASSIGNED"}`) |
| `/api/collectors` | GET, POST | List/register collectors |
| `/api/collectors/{id}` | GET, PUT | Read/update collector details |
| `/api/collectors/available` | GET | Available collectors |
| `/api/collectors/pending-reports` | GET | Feign-backed pending reports |
| `/api/collectors/assignments` | POST | Assign collector (`collectorId`, `wasteReportId`) |
| `/api/collectors/assignments/report/{reportId}` | GET | Active report assignment |
| `/api/collectors/assignments/{id}` | GET | Assignment details |
| `/api/collectors/{id}/assignments` | GET | Collector assignments |
| `/api/collectors/assignments/{id}/status` | PATCH | Complete/cancel assignment (`{"status":"COMPLETED"}`) |
| `/api/pickups` | GET, POST | List/create pickups |
| `/api/pickups/{id}` | GET | Pickup details |
| `/api/pickups/{id}/start` | PATCH | Start a scheduled pickup |
| `/api/pickups/{id}/complete` | PATCH | Complete and record actual quantity |
| `/api/pickups/{id}/handover` | POST | Record recycling handover |
| `/api/pickups/{id}/cancel` | PATCH | Cancel a not-yet-completed pickup |

Business services also run directly on ports 8081–8083 for isolated debugging. Use gateway paths for the integrated demonstration.

## Postman walkthrough

Set Postman base URL to `http://localhost:8080/api`. Send JSON with header `Content-Type: application/json`. Capture each returned `id` for the next request.

1. Create a report: `POST {{baseUrl}}/waste`

```json
{"reporterName":"Asha Kumar","reporterType":"HOUSEHOLD","wasteCategory":"PLASTIC","quantity":5.5,"pickupAddress":"12 Lake Road","description":"Clean bottles"}
```

2. Read reports: `GET {{baseUrl}}/waste`; then `GET {{baseUrl}}/waste/1`. To edit while pending, `PUT /waste/1` with the same report fields. To test pending lookup, `GET /collectors/pending-reports`.
3. Register collector: `POST /collectors`

```json
{"name":"Ravi Singh","phoneNumber":"9876543210","vehicleNumber":"KA-01-AB-1234"}
```

4. Assign: `POST /collectors/assignments`

```json
{"collectorId":1,"wasteReportId":1}
```

Confirm with `GET /collectors/assignments/report/1`, `GET /collectors/1/assignments`, and `GET /waste/1` (status `ASSIGNED`). A second assignment for the same report should return HTTP 409.
5. Create a schedule: `POST /pickups`

```json
{"wasteReportId":1,"collectorId":1,"pickupDate":"2026-12-01","pickupTime":"09:30:00","pickupAddress":"12 Lake Road","scheduledQuantity":5.5}
```

6. List pickups: `GET /pickups`. Start: `PATCH /pickups/1/start` (no body). Complete: `PATCH /pickups/1/complete` with `{"actualCollectedQuantity":5.0}`. Check `/waste/1` status `COLLECTED`.
7. Handover: `POST /pickups/1/handover`

```json
{"organizationName":"GreenCycle Recycling","quantityTransferred":4.8}
```

Confirm pickup status `HANDED_OVER`, report status `HANDED_OVER`, and that a second handover is rejected. Try invalid enums, nonpositive quantities, completion before start, and scheduling without an assignment to see validation and HTTP error responses.

The date in the sample is illustrative; use a future date for your own demo. For a clean database demonstration, stop the services and remove only that service's `data` directory.

## Workflow and design concepts (viva notes)

1. A household or business submits a report; the Waste Reporting Service stores it as `PENDING`.
2. Collector Management reads pending reports through Feign, checks that the report is still pending, prevents duplicates, claims an available collector, creates an assignment, and asks Waste Reporting to mark it `ASSIGNED`.
3. Pickup Scheduling verifies the active assignment and report status through Feign, creates a pickup, and changes the report to `SCHEDULED`.
4. Starting a pickup moves it to `IN_PROGRESS`. Completion records actual kilograms, changes pickup to `COMPLETED`, and updates the report to `COLLECTED`.
5. Handover records the organization, category, quantity, and date, then changes pickup and report to `HANDED_OVER`.

**Why microservices?** They separate business capabilities into independently runnable and understandable applications. This adds network calls and deployment work, so it is useful here to demonstrate service boundaries rather than claim it is always simpler than a monolith. There are three business services because reports, collectors/assignments, and schedules/recycling records have distinct ownership and data.

**API Gateway** is the frontend's single entry point. Its `lb://SERVICE-ID` routes use Spring Cloud LoadBalancer and Eureka, so UI code does not need service ports. **Eureka** is a registry: services register their application name and network location on startup; clients discover locations from the registry. **OpenFeign** turns annotated Java interfaces into HTTP clients; the interface uses a Eureka service name, not a hard-coded host.

**H2** is a lightweight Java relational database suitable for a local lab. File mode retains data between process restarts. **Spring Data JPA** supplies repository APIs such as `findAll` and `findByStatus`; **Hibernate** is the JPA implementation that maps entities to tables and performs SQL. Each service owns its own database. The console is for local development only; it is not exposed through the gateway.

**Layer roles:** Controller maps HTTP endpoints and request data; Service owns business rules and transaction boundaries; Repository performs database access; Entity maps a table; DTO/record types define request data; Feign client calls another service. Constructor injection makes dependencies explicit.

**Useful annotations:** `@SpringBootApplication` enables auto-configuration/component scanning; `@RestController` returns response bodies; `@RequestMapping` groups routes; `@GetMapping`/`@PostMapping`/`@PutMapping`/`@PatchMapping`/`@DeleteMapping` map HTTP verbs; `@Service` and `@Repository` identify layers; `@Entity`, `@Id`, `@GeneratedValue` map persistence; `@Valid`, `@NotBlank`, `@NotNull`, `@Positive` validate input; `@Transactional` groups a service's local database work; `@FeignClient` declares a service client; `@EnableFeignClients` scans those clients; `@EnableEurekaServer` starts the registry.

**Properties files:** Each application properties file sets a distinct name and port plus Eureka location. The three business services additionally set their own file-based JDBC URL, H2 driver, `sa` credentials, Hibernate schema update, SQL logging, and H2 console path. The gateway properties define path predicates and service-ID URIs. Eureka disables self-registration and registry fetching for standalone use.

### Sample viva questions and answers

- **What is service discovery?** A registry lets a client find a service instance by logical application name instead of a manually configured IP address.
- **Why use a gateway?** It centralizes the frontend entry point and routes each API path to its owning service.
- **Does each service share one database?** No. Each business service owns a separate H2 file and exposes data via APIs.
- **What does Feign do?** It generates the HTTP implementation for a Java interface annotated with remote endpoint mappings.
- **What is JPA vs Hibernate?** JPA is a persistence specification; Hibernate is an implementation of it.
- **What is the purpose of a repository?** It provides database operations without putting SQL concerns in controllers.
- **What does `@Transactional` do here?** It makes local database mutations within a service method commit or roll back together; it cannot atomically include remote HTTP services.
- **Why validate transitions?** It prevents steps such as handing over a pickup before collection or assigning a cancelled report.
- **Why use file-based H2?** It preserves lab data across restarts while avoiding an external database server.
- **What happens if a service is unavailable?** Feign cannot complete the request; callers receive an error. This simple project does not add retries or a circuit breaker.

## Implementation notes

Spring Boot `3.3.5` with Spring Cloud `2023.0.3` uses Java 17 and a compatible Spring Cloud release train. The application is intentionally synchronous and local-first for straightforward lab explanation. For a production system, add authentication, stronger validation and observability, retries/timeouts, database migration tooling, and reliable cross-service event handling.
