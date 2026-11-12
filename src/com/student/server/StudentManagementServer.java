package com.student.server;

import com.student.dao.StudentDAO;
import com.student.model.Student;
import com.student.util.DBConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * StudentManagementServer.java
 * 
 * Standalone Java Web Server for the Student Management System.
 * Built using Java standard library (com.sun.net.httpserver.HttpServer).
 * 
 * Benefits for Beginners:
 * - Runs directly with standard Java! No need to install, configure, or debug external Tomcat servers.
 * - Serves frontend static assets (HTML, CSS, JS).
 * - Connects to MySQL via StudentDAO / JDBC.
 * - Displays an interactive console log with real-time operations.
 */
public class StudentManagementServer {

    private static final int DEFAULT_PORT = 8080;
    private static final StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        HttpServer server = null;

        // Attempt to bind to default port 8080, or 8081 if busy
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            port = 8081;
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (IOException ex) {
                System.err.println("[Server Error] Failed to start server on ports 8080 and 8081: " + ex.getMessage());
                return;
            }
        }

        // Register HTTP Endpoints
        server.createContext("/api/students", new StudentApiHandler());
        server.createContext("/", new StaticFileHandler());

        // Multi-threaded execution pool
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        // Welcome Banner
        printBanner(port);
    }

    private static void printBanner(int port) {
        System.out.println("==================================================================");
        System.out.println("      STUDENT MANAGEMENT SYSTEM - JAVA WEB APPLICATION            ");
        System.out.println("==================================================================");
        System.out.println("  * Web Server Status : RUNNING");
        System.out.println("  * Local URL         : http://localhost:" + port);
        System.out.println("  * MySQL Status      : " + (DBConnection.isMySQLAvailable() ? "CONNECTED (MySQL 8.0)" : "OFFLINE (Using Built-in Data Store)"));
        System.out.println("  * REST API Base     : http://localhost:" + port + "/api/students");
        System.out.println("==================================================================");
        System.out.println("  Press Ctrl+C in this terminal window to stop the application.");
        System.out.println("==================================================================\n");
    }

    /**
     * Handler for the Student REST API (/api/students)
     */
    static class StudentApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS headers for smooth local development
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Action");

            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(exchange);
                } else {
                    sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getRawQuery();
            Map<String, String> params = parseQueryParams(query);

            String idParam = params.get("id");
            String action = params.get("action");

            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idParam.trim());
                    Student s = studentDAO.getStudentById(id);
                    if (s != null) {
                        sendJsonResponse(exchange, 200, s.toJson());
                    } else {
                        sendJsonResponse(exchange, 404, "{\"error\":\"Student not found\"}");
                    }
                } catch (NumberFormatException e) {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Invalid ID format\"}");
                }
            } else if ("search".equalsIgnoreCase(action)) {
                String q = params.get("query");
                String dept = params.get("department");
                String yr = params.get("year");

                List<Student> list = studentDAO.searchStudents(q, dept, yr);
                sendJsonResponse(exchange, 200, studentsToJson(list));
            } else {
                List<Student> list = studentDAO.getAllStudents();
                sendJsonResponse(exchange, 200, studentsToJson(list));
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            // Read request body
            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            String body = new String(buffer.toByteArray(), StandardCharsets.UTF_8);

            Map<String, String> params = parseFormBody(body);
            String action = params.get("action");

            if (action == null || action.trim().isEmpty()) {
                String headerAction = exchange.getRequestHeaders().getFirst("X-Action");
                if (headerAction != null) action = headerAction;
            }

            if ("add".equalsIgnoreCase(action)) {
                String name = params.get("name");
                String regNo = params.get("regNo");
                String email = params.get("email");
                String phone = params.get("phone");
                String dept = params.get("department");
                String year = params.get("yearOfStudy");

                if (isEmpty(name) || isEmpty(regNo) || isEmpty(email) || isEmpty(phone) || isEmpty(dept) || isEmpty(year)) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"All fields are required!\"}");
                    return;
                }

                if (studentDAO.getStudentByRegNo(regNo) != null) {
                    sendJsonResponse(exchange, 409, "{\"success\":false,\"message\":\"Register number '" + regNo + "' is already registered!\"}");
                    return;
                }

                Student s = new Student(name.trim(), regNo.trim(), email.trim(), phone.trim(), dept.trim(), year.trim());
                boolean created = studentDAO.addStudent(s);

                if (created) {
                    sendJsonResponse(exchange, 201, "{\"success\":true,\"message\":\"Student added successfully!\",\"student\":" + s.toJson() + "}");
                } else {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Failed to save student.\"}");
                }

            } else if ("update".equalsIgnoreCase(action)) {
                String idStr = params.get("id");
                String name = params.get("name");
                String regNo = params.get("regNo");
                String email = params.get("email");
                String phone = params.get("phone");
                String dept = params.get("department");
                String year = params.get("yearOfStudy");

                if (isEmpty(idStr) || isEmpty(name) || isEmpty(regNo) || isEmpty(email) || isEmpty(phone) || isEmpty(dept) || isEmpty(year)) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"All fields are required!\"}");
                    return;
                }

                int id = Integer.parseInt(idStr.trim());
                Student existing = studentDAO.getStudentById(id);
                if (existing == null) {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}");
                    return;
                }

                Student regCheck = studentDAO.getStudentByRegNo(regNo);
                if (regCheck != null && regCheck.getId() != id) {
                    sendJsonResponse(exchange, 409, "{\"success\":false,\"message\":\"Register number belongs to another student!\"}");
                    return;
                }

                Student updated = new Student(id, name.trim(), regNo.trim(), email.trim(), phone.trim(), dept.trim(), year.trim());
                boolean success = studentDAO.updateStudent(updated);

                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student details updated successfully!\",\"student\":" + updated.toJson() + "}");
                } else {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Failed to update student.\"}");
                }

            } else if ("delete".equalsIgnoreCase(action)) {
                String idStr = params.get("id");
                if (isEmpty(idStr)) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Student ID is required.\"}");
                    return;
                }

                int id = Integer.parseInt(idStr.trim());
                boolean deleted = studentDAO.deleteStudent(id);

                if (deleted) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student deleted successfully!\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Student record could not be found or deleted.\"}");
                }

            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Unknown action: " + action + "\"}");
            }
        }
    }

    /**
     * Handler for Static Frontend Files (HTML, CSS, JS, etc.)
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Look in current working directory "web" folder, or fallback to relative path
            File file = new File("web" + path);
            if (!file.exists()) {
                // Also check src/main/webapp if configured
                file = new File("src/main/webapp" + path);
            }

            if (!file.exists() || file.isDirectory()) {
                String notFoundMsg = "<h1>404 Not Found</h1><p>Resource " + path + " was not found.</p>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFoundMsg.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFoundMsg.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, file.length());

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }

        private String getContentType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }

    // --- UTILITY METHODS ---

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }

    private static Map<String, String> parseFormBody(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        // Check if body is in JSON format
        if (body.trim().startsWith("{") && body.trim().endsWith("}")) {
            return parseSimpleJson(body);
        }

        // Form urlencoded format
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length > 0) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                map.put(key, val);
            }
        }
        return map;
    }

    /**
     * Parses simple flat JSON payloads without external libraries
     */
    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        String content = json.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

        String[] tokens = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String token : tokens) {
            String[] kv = token.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String key = cleanJsonToken(kv[0]);
                String val = cleanJsonToken(kv[1]);
                map.put(key, val);
            }
        }
        return map;
    }

    private static String cleanJsonToken(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String studentsToJson(List<Student> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
