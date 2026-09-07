# java-hello-world-app

Spring Boot 3.4 + Java 21 app demonstrating database-backed HTTP, idempotent schema migration, and dual entry-point JAR packaging on Zerops.

## Zerops service facts

- HTTP port: `8080`
- Siblings: `db` (PostgreSQL) — env: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `DB_NAME`
- Runtime base: `java@21`

## Zerops dev

`setup: dev` idles on `zsc noop --silent`; the agent starts the dev server.

- Dev command: `mvn spring-boot:run`
- In-container rebuild without deploy: `mvn clean package -DskipTests`

**All platform operations (start/stop/status/logs of the dev server, deploy, env / scaling / storage / domains) go through the Zerops development workflow via `zcp` MCP tools. Don't shell out to `zcli`.**

## Notes

- The same `target/app.jar` serves both the web server and the migration runner — `pom.xml` uses ZIP layout (PropertiesLauncher) and `-Dloader.main=io.zerops.recipe.Migrate` switches entry points without rebuilding.
- Schema is managed by `Migrate.java` via `zsc execOnce` in `initCommands`, not by JPA (`ddl-auto=none`).
- `java -jar target/app.jar` also works if you just want to run the pre-built JAR without `mvn`.
