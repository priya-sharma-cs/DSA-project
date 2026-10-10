package com.queue.project;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * QueueServer provides an HTTP Web Server and REST API for the Queue Visualizer.
 *
 * Endpoints:
 * - GET    /              : Serves modern web frontend
 * - GET    /api/queue     : Returns current queue elements, size, and status
 * - POST   /api/enqueue   : Enqueues a new item (body: {"value":"..."})
 * - POST   /api/dequeue   : Dequeues and returns the front item
 * - GET    /api/peek      : Inspects the front item without removing it
 * - POST   /api/clear     : Clears all items in the active queue
 * - GET    /api/history   : Retrieves database audit log
 * - DELETE /api/history   : Truncates database audit log
 */
public class QueueServer {

    // Shared thread-safe custom queue instance
    private static final CustomQueue queue = AppMain.queue;

    public static void main(String[] args) throws Exception {
        // Attempt optional database initialization
        DatabaseManager.createTable();

        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Frontend static routes
        server.createContext("/", QueueServer::serveFrontend);

        // REST API routes
        server.createContext("/api/queue", QueueServer::handleQueue);
        server.createContext("/api/enqueue", QueueServer::handleEnqueue);
        server.createContext("/api/dequeue", QueueServer::handleDequeue);
        server.createContext("/api/peek", QueueServer::handlePeek);
        server.createContext("/api/clear", QueueServer::handleClear);
        server.createContext("/api/history", QueueServer::handleHistory);

        // Default thread executor
        server.setExecutor(null);

        System.out.println("==================================================");
        System.out.println("   QUEUE VISUALIZER SERVER STARTED SUCCESSFULLY   ");
        System.out.println("==================================================");
        System.out.println(" Web UI URL : http://localhost:" + port);
        System.out.println(" Data Struct: Custom Linked-List Queue (O(1) FIFO)");
        System.out.println(" Database   : " + (DatabaseManager.isDatabaseAvailable() ? "MySQL Active" : "Offline (In-Memory Fallback)"));
        System.out.println("==================================================");

        server.start();
    }

    // ==================================================
    // STATIC FILE SERVING
    // ==================================================

    private static void serveFrontend(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Prevent directory traversal attacks
        if (path.contains("..")) {
            sendResponse(exchange, 403, "Access denied");
            return;
        }

        Path file = resolveWebFile(path);

        if (!Files.exists(file) || Files.isDirectory(file)) {
            sendResponse(exchange, 404, "File not found: " + path);
            return;
        }

        byte[] data = Files.readAllBytes(file);
        String contentType = getContentType(file.getFileName().toString());

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(200, data.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }

    /**
     * Resolves web asset files whether the JVM is started from QueueDemo/
     * or the parent workspace folder.
     */
    private static Path resolveWebFile(String requestPath) {
        if (requestPath.startsWith("/")) {
            requestPath = requestPath.substring(1);
        }

        // 1. Direct web/ directory check (started from QueueDemo)
        Path candidate1 = Paths.get("web", requestPath);
        if (Files.isRegularFile(candidate1)) {
            return candidate1;
        }

        // 2. QueueDemo/web/ check (started from parent folder)
        Path candidate2 = Paths.get("QueueDemo", "web", requestPath);
        if (Files.isRegularFile(candidate2)) {
            return candidate2;
        }

        // 3. Resolve relative to compiled bytecode location
        try {
            Path codeLoc = Paths.get(QueueServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path projectRoot = Files.isDirectory(codeLoc) ? codeLoc.getParent() : codeLoc.getParent().getParent();
            if (projectRoot != null) {
                Path candidate3 = projectRoot.resolve("web").resolve(requestPath);
                if (Files.isRegularFile(candidate3)) {
                    return candidate3;
                }
            }
        } catch (Exception ignored) {}

        return candidate1;
    }

    // ==================================================
    // API: GET /api/queue
    // ==================================================

    private static void handleQueue(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        List<String> items = queue.toList();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"queue\":[");

        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(DatabaseManager.escapeJson(items.get(i))).append("\"");
        }
        json.append("],");

        json.append("\"size\":").append(items.size()).append(",");
        json.append("\"isEmpty\":").append(items.isEmpty()).append(",");
        json.append("\"front\":").append(items.isEmpty() ? "null" : "\"" + DatabaseManager.escapeJson(items.get(0)) + "\"").append(",");
        json.append("\"rear\":").append(items.isEmpty() ? "null" : "\"" + DatabaseManager.escapeJson(items.get(items.size() - 1)) + "\"").append(",");
        json.append("\"databaseConnected\":").append(DatabaseManager.isDatabaseAvailable());
        json.append("}");

        sendResponse(exchange, 200, json.toString());
    }

    // ==================================================
    // API: POST /api/enqueue
    // ==================================================

    private static void handleEnqueue(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String value = extractJsonString(body, "value");

        if (value == null || value.trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Value is required and cannot be blank.\"}");
            return;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() > 255) {
            sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Value exceeds maximum length of 255 characters.\"}");
            return;
        }

        queue.enqueue(trimmedValue);
        DatabaseManager.saveOperation("ENQUEUE", trimmedValue);

        String response = String.format(
                "{\"success\":true,\"message\":\"Enqueued: %s\",\"size\":%d}",
                DatabaseManager.escapeJson(trimmedValue),
                queue.size()
        );
        sendResponse(exchange, 200, response);
    }

    // ==================================================
    // API: POST /api/dequeue
    // ==================================================

    private static void handleDequeue(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        if (queue.isEmpty()) {
            sendResponse(exchange, 200, "{\"success\":false,\"message\":\"Queue is empty.\",\"value\":null,\"size\":0}");
            return;
        }

        String removed = queue.dequeue();
        DatabaseManager.saveOperation("DEQUEUE", removed);

        String response = String.format(
                "{\"success\":true,\"message\":\"Dequeued: %s\",\"value\":\"%s\",\"size\":%d}",
                DatabaseManager.escapeJson(removed),
                DatabaseManager.escapeJson(removed),
                queue.size()
        );
        sendResponse(exchange, 200, response);
    }

    // ==================================================
    // API: GET /api/peek
    // ==================================================

    private static void handlePeek(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        if (queue.isEmpty()) {
            sendResponse(exchange, 200, "{\"success\":false,\"message\":\"Queue is empty.\",\"value\":null}");
            return;
        }

        String front = queue.peek();
        String response = String.format(
                "{\"success\":true,\"message\":\"Front: %s\",\"value\":\"%s\"}",
                DatabaseManager.escapeJson(front),
                DatabaseManager.escapeJson(front)
        );
        sendResponse(exchange, 200, response);
    }

    // ==================================================
    // API: POST /api/clear
    // ==================================================

    private static void handleClear(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        queue.clear();
        sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Queue cleared successfully.\",\"size\":0}");
    }

    // ==================================================
    // API: /api/history (GET / DELETE)
    // ==================================================

    private static void handleHistory(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("GET".equalsIgnoreCase(method)) {
            String history = DatabaseManager.getHistoryJson();
            String response = String.format(
                    "{\"history\":%s,\"databaseConnected\":%b}",
                    history,
                    DatabaseManager.isDatabaseAvailable()
            );
            sendResponse(exchange, 200, response);
            return;
        }

        if ("DELETE".equalsIgnoreCase(method)) {
            boolean cleared = DatabaseManager.clearHistory();
            if (cleared) {
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Queue history cleared from database.\"}");
            } else {
                sendResponse(exchange, 200, "{\"success\":false,\"message\":\"Database offline. No history to clear.\"}");
            }
            return;
        }

        sendResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
    }

    // ==================================================
    // ROBUST JSON STRING PARSER (RFC 8259 Compliant)
    // ==================================================

    public static String extractJsonString(String json, String targetKey) {
        if (json == null) return null;
        String keySearch = "\"" + targetKey + "\"";
        int keyIndex = json.indexOf(keySearch);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(':', keyIndex + keySearch.length());
        if (colonIndex == -1) return null;

        int openQuote = -1;
        for (int i = colonIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (c == '"') {
                openQuote = i;
                break;
            }
            return null; // Value is not string
        }

        if (openQuote == -1) return null;

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        int closeQuote = -1;

        for (int i = openQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                switch (c) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            try {
                                String hex = json.substring(i + 1, i + 5);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException ignored) {
                                sb.append('u');
                            }
                        } else {
                            sb.append('u');
                        }
                    }
                    default -> sb.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                closeQuote = i;
                break;
            } else {
                sb.append(c);
            }
        }

        if (closeQuote == -1) return null;
        return sb.toString();
    }

    // ==================================================
    // HELPERS
    // ==================================================

    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }

    private static String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filename.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (filename.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (filename.endsWith(".svg"))  return "image/svg+xml";
        if (filename.endsWith(".png"))  return "image/png";
        if (filename.endsWith(".ico"))  return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }
}