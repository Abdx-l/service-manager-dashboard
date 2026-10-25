# Service Manager Dashboard

Spring Boot container start/stop/restart API with an explicit container allowlist, Docker log tails, Actuator polling, public read-only STOMP status updates, and a React control panel with operation confirmation.

## Run

Java 17/Maven and Docker required. Set ADMIN_PASSWORD, SERVICE_CONTAINERS (comma-separated names), and HEALTH_URLS (name=http://host/actuator/health pairs). Run `mvn spring-boot:run`; UI: `cd web && npm install && npm run dev`. The Docker endpoint must be accessible to the server process.
