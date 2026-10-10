package com.queue.project;

import java.sql.*;

/**
 * DatabaseManager handles optional MySQL persistence for queue operation history.
 *
 * Fault Tolerance:
 * - Queue operations operate purely in-memory in CustomQueue.
 * - If MySQL is offline or unconfigured, all queue operations continue seamlessly.
 * - Credentials are read from environment variables with safe local defaults.
 *   Passwords are never hardcoded or exposed in logs.
 */
public class DatabaseManager {

    private static final String DB_HOST =
            System.getenv().getOrDefault("DB_HOST", "localhost");

    private static final String DB_PORT =
            System.getenv().getOrDefault("DB_PORT", "3306");

    private static final String DB_NAME =
            System.getenv().getOrDefault("DB_NAME", "queuedb");

    private static final String URL =
            System.getenv().getOrDefault(
                    "DB_URL",
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            );

    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "root");

    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "");

    private static volatile boolean dbInitialized = false;
    private static volatile boolean dbAvailable = false;

    /**
     * Establishes a database connection using configured parameters.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Returns true if MySQL was successfully reached during the last check.
     */
    public static boolean isDatabaseAvailable() {
        return dbAvailable;
    }

    /**
     * Initializes the database table if MySQL is reachable.
     * Fails gracefully without throwing exceptions if MySQL is stopped.
     */
    public static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS queue_history (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    operation VARCHAR(50),
                    value VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (
                Connection con = getConnection();
                Statement stmt = con.createStatement()
        ) {
            stmt.executeUpdate(sql);
            dbAvailable = true;
            dbInitialized = true;
            System.out.println("[Database] MySQL connected successfully. Table 'queue_history' ready.");
        } catch (SQLException e) {
            dbAvailable = false;
            dbInitialized = true;
            System.out.println("[Database Notice] MySQL is offline or queuedb is not found. History logging disabled; Queue is running purely in-memory.");
        }
    }

    /**
     * Records an enqueue or dequeue operation in the database history table.
     * Silently ignores persistence failure if MySQL is offline.
     */
    public static void saveOperation(String operation, String value) {
        String sql = """
                INSERT INTO queue_history(operation, value)
                VALUES (?, ?)
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, operation);
            ps.setString(2, value);
            ps.executeUpdate();
            dbAvailable = true;
        } catch (SQLException e) {
            dbAvailable = false;
            // Graceful silent fallback so in-memory queue operations are not delayed or crashed
        }
    }

    /**
     * Displays operation history in the console (used by AppMain CLI).
     */
    public static void showHistory() {
        String sql = "SELECT * FROM queue_history ORDER BY id DESC";

        try (
                Connection con = getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            dbAvailable = true;
            System.out.println("\n========== QUEUE DATABASE HISTORY ==========");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(
                        "ID: " + rs.getInt("id") +
                        " | Operation: " + rs.getString("operation") +
                        " | Value: " + rs.getString("value") +
                        " | Time: " + rs.getTimestamp("created_at")
                );
            }
            if (!found) {
                System.out.println("No history records found in database.");
            }
            System.out.println("============================================");
        } catch (SQLException e) {
            dbAvailable = false;
            System.out.println("[Database Notice] Cannot display history: MySQL is offline or unreachable.");
        }
    }

    /**
     * Retrieves operation history serialized as JSON for the web API.
     * Returns an empty JSON array if MySQL is offline.
     */
    public static String getHistoryJson() {
        String sql = "SELECT * FROM queue_history ORDER BY id DESC LIMIT 100";

        StringBuilder json = new StringBuilder("[");

        try (
                Connection con = getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            dbAvailable = true;
            boolean first = true;

            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;

                int id = rs.getInt("id");
                String operation = escapeJson(rs.getString("operation"));
                String value = escapeJson(rs.getString("value"));
                String time = escapeJson(String.valueOf(rs.getTimestamp("created_at")));

                json.append("{")
                    .append("\"id\":").append(id).append(",")
                    .append("\"operation\":\"").append(operation).append("\",")
                    .append("\"value\":\"").append(value).append("\",")
                    .append("\"time\":\"").append(time).append("\"")
                    .append("}");
            }

            json.append("]");
            return json.toString();

        } catch (SQLException e) {
            dbAvailable = false;
            return "[]";
        }
    }

    /**
     * Clears all records from the database history table.
     */
    public static boolean clearHistory() {
        String sql = "TRUNCATE TABLE queue_history";

        try (
                Connection con = getConnection();
                Statement stmt = con.createStatement()
        ) {
            stmt.executeUpdate(sql);
            dbAvailable = true;
            System.out.println("[Database] Queue history truncated.");
            return true;
        } catch (SQLException e) {
            // Try DELETE fallback if TRUNCATE has foreign keys/permissions issue
            try (
                    Connection con = getConnection();
                    Statement stmt = con.createStatement()
            ) {
                stmt.executeUpdate("DELETE FROM queue_history");
                dbAvailable = true;
                return true;
            } catch (SQLException ignored) {
                dbAvailable = false;
                return false;
            }
        }
    }

    /**
     * Safe JSON string escaper according to RFC 8259.
     */
    public static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}