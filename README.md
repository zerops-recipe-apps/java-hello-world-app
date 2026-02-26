# Java Hello World Recipe App

<!-- #ZEROPS_EXTRACT_START:intro# -->
[Java 21](https://www.java.com) web application built with [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Data JPA](https://spring.io/projects/spring-data-jpa), connecting to a [PostgreSQL](https://www.postgresql.org) database. The app demonstrates idempotent schema migration, a database-backed health check at `/`, and dual entry-point packaging using Spring Boot's PropertiesLauncher — a single JAR serves both the web server and the migration runner.
Used within [Java Hello World recipe](https://app.zerops.io/recipes/java-hello-world) for [Zerops](https://zerops.io) platform.
<!-- #ZEROPS_EXTRACT_END:intro# -->

⬇️ **Full recipe page and deploy with one-click**

[![Deploy on Zerops](https://github.com/zeropsio/recipe-shared-assets/blob/main/deploy-button/light/deploy-button.svg)](https://app.zerops.io/recipes/java-hello-world?environment=small-production)

![java cover](https://github.com/zeropsio/recipe-shared-assets/blob/main/covers/svg/cover-java.svg)

## Integration Guide

<!-- #ZEROPS_EXTRACT_START:integration-guide# -->

### 1. Adding `zerops.yaml`
The main application configuration file you place at the root of your repository, it tells Zerops how to build, deploy and run your application.

```yaml
# The 'prod' setup compiles an optimized Spring Boot fat JAR
# for deployment. The 'dev' setup ships source code alongside
# the pre-built JAR so developers can SSH in, edit files, and
# rebuild freely using the pre-installed mvn and java tools.
zerops:
  - setup: prod
    build:
      base: java@21

      # Compile and package the Spring Boot fat JAR.
      # '-DskipTests' is intentional — integration tests
      # belong in CI pipelines, not in the Zerops build
      # container. Maven 3.9 is pre-installed on java@21.
      buildCommands:
        - mvn clean package -DskipTests

      # One file covers both the app and migration entry points.
      # ZIP layout in pom.xml enables PropertiesLauncher, which
      # reads -Dloader.main at runtime to switch between them.
      deployFiles:
        - target/app.jar

      # Maven resolves dependencies into ~/.m2 (outside the build
      # directory). 'cache: true' snapshots the build container
      # image — including ~/.m2 — so subsequent builds skip
      # downloading the dependency graph from Maven Central.
      cache: true

    # Zerops runs the readiness check after each new runtime
    # container starts and before it receives traffic from the
    # project balancer. Containers that fail are replaced,
    # not promoted.
    deploy:
      readinessCheck:
        httpGet:
          port: 8080
          path: /

    run:
      base: java@21

      # Run the migration exactly once per deployed version.
      # 'zsc execOnce ${appVersionId}' ensures a single container
      # executes even when minContainers > 1 — others wait.
      # In initCommands (not buildCommands) so migration and
      # new code are always deployed together atomically.
      #
      # PropertiesLauncher (ZIP layout) reads -Dloader.main
      # to invoke Migrate.main() directly — no Spring context
      # is created, just a plain JDBC connection.
      initCommands:
        - zsc execOnce ${appVersionId} -- java -Dloader.main=io.zerops.recipe.Migrate -jar target/app.jar

      ports:
        - port: 8080
          httpSupport: true

      # Env vars follow '{hostname}_{credential}' — for the 'db'
      # service: db_hostname, db_port, db_user, db_password.
      # DB_NAME matches the database name Zerops creates (same
      # as the service hostname).
      envVariables:
        DB_NAME: db
        DB_HOST: ${db_hostname}
        DB_PORT: ${db_port}
        DB_USER: ${db_user}
        DB_PASS: ${db_password}

      start: java -jar target/app.jar

  - setup: dev
    build:
      base: java@21

      # Build the fat JAR during the build phase so that
      # target/app.jar is available for the initCommands
      # migration and for quick test runs after SSH.
      buildCommands:
        - mvn clean package -DskipTests

      # Deploy source for editing and the compiled JAR for
      # running migrations and the app. target/ is excluded
      # to avoid shipping hundreds of MB of build artifacts —
      # only app.jar is needed. Developers rebuild with
      # 'mvn package' or run directly with 'mvn spring-boot:run'.
      deployFiles:
        - ./src
        - ./pom.xml
        - ./zerops.yaml
        - target/app.jar

      cache: true

    run:
      base: java@21

      # Migration runs identically to prod — same JAR,
      # same zsc execOnce guard.
      initCommands:
        - zsc execOnce ${appVersionId} -- java -Dloader.main=io.zerops.recipe.Migrate -jar target/app.jar

      ports:
        - port: 8080
          httpSupport: true

      envVariables:
        DB_NAME: db
        DB_HOST: ${db_hostname}
        DB_PORT: ${db_port}
        DB_USER: ${db_user}
        DB_PASS: ${db_password}

      # Dev container stays idle. SSH in and use the pre-installed
      # tools: 'mvn spring-boot:run' for hot-reload development,
      # or 'java -jar target/app.jar' to run the compiled binary.
      # Database is already migrated and ready when you connect.
      start: zsc noop --silent
```
<!-- #ZEROPS_EXTRACT_END:integration-guide# -->
