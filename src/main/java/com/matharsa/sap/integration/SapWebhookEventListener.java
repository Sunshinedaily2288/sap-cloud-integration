package com.matharsa.sap.integration;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class SapWebhookEventListener {

    // Assigning dedicated port 8098 out of your portfolio tracking matrix
    private static final int PORT = 8098;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("INITIALIZING CAPSTONE LAYER: SAP Cloud Webhook Server");
        System.out.println("==================================================");

        try {
            // 1. Instantiate an internal HTTP Server bound to your portfolio Port 8098
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // 2. Establish a dedicated URL context pathway to intercept incoming SAP events
            server.createContext("/api/v1/sap-events", new SapNotificationHandler());

            // 3. Assign an enterprise executor thread pool to process parallel requests
            server.setExecutor(Executors.newFixedThreadPool(4));

            System.out.println("Starting embedded HTTP microserver...");
            server.start();

            System.out.println("\n🚀 BOUND SUCCESS: Webhook listener active on Port: " + PORT);
            System.out.println("Listening for real-time mesh events at: http://localhost:" + PORT + "/api/v1/sap-events");
            System.out.println("Press Stop in IntelliJ anytime to shut down the server network lane.");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("CRITICAL SERVER FAULT: Failed to open network port: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Custom HTTP Handler mapping class to evaluate network payloads on the fly
    static class SapNotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("\n⚡ [NETWORK SIGNAL DETECTED]: Incoming packet hitting webhook boundary...");

            // Strict routing evaluation: Ensure the external system sent a proper POST packet
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                System.out.println("⚠️ BLOCK: Invalid request method token: " + exchange.getRequestMethod());
                String errorMsg = "Method Not Allowed. Use HTTP POST to dispatch events.";
                exchange.sendResponseHeaders(405, errorMsg.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorMsg.getBytes());
                }
                return;
            }

            // Ingest and convert the incoming stream payload into a local readable string
            StringBuilder payloadBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    payloadBuilder.append(currentLine);
                }
            }

            String incomingJsonPayload = payloadBuilder.toString();
            System.out.println("📥 EVENT PACKET INGESTED:");
            System.out.println(incomingJsonPayload);

            // Construct an instant confirmation message to return to the dispatcher system
            String responseBody = "{\"status\":\"ACKNOWLEDGED\",\"message\":\"Event captured. Processing pipelines triggered.\"}";

            // Set standard content parameters signaling a clean JSON acknowledgement transaction state back
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(202, responseBody.length()); // 202 Accepted means queued for processing

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("✅ DISPATCHED: HTTP 202 Acknowledged packet sent back to event mesh.");
        }
    }
}
