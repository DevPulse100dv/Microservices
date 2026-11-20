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
