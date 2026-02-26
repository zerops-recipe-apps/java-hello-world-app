package io.zerops.recipe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Standalone database migration — invoked via initCommands in zerops.yaml:
 *
 *   zsc execOnce ${appVersionId} -- \
 *     java -Dloader.main=io.zerops.recipe.Migrate -jar target/app.jar
 *
 * PropertiesLauncher (ZIP layout) routes -Dloader.main to this class.
 * No Spring context is created — plain JDBC only.
 *
 * Idempotent: IF NOT EXISTS and ON CONFLICT DO NOTHING make repeated
 * runs safe, though zsc execOnce already prevents that per version.
 */
public class Migrate {

    public static void main(String[] args) throws Exception {
        String host     = System.getenv("DB_HOST");
        String port     = System.getenv("DB_PORT") != null
                            ? System.getenv("DB_PORT") : "5432";
        String dbName   = System.getenv("DB_NAME");
        String user     = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");

        String url = "jdbc:postgresql://" + host + ":" + port
                   + "/" + dbName + "?sslmode=disable";

        System.out.println("Running migration against " + host + ":" + port + "/" + dbName);

        try (Connection conn   = DriverManager.getConnection(url, user, password);
             Statement  stmt   = conn.createStatement()) {

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS greetings (" +
                "  id      INTEGER PRIMARY KEY," +
                "  message TEXT    NOT NULL" +
                ")"
            );

            stmt.execute(
                "INSERT INTO greetings (id, message)" +
                " VALUES (1, 'Hello from Zerops!')" +
                " ON CONFLICT (id) DO NOTHING"
            );

            System.out.println("Migration completed successfully.");
        }
    }
}
