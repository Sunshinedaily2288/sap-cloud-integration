package com.matharsa.sap.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SapSecureIntegrationEngine {

    public static void main(String[] args) {
        // Target Server Endpoints on Port 8097
        String authEndpoint = "http://localhost:8097/api/auth/token";
        String ordersEndpoint = "http://localhost:8097/api/sap/orders";

        System.out.println("==================================================");
        System.out.println("INITIALIZING ENTERPRISE SECURITY PIPELINE");
        System.out.println("==================================================");

        try {
            // Build our central HTTP routing client
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            ObjectMapper jsonMapper = new ObjectMapper();

            // ----------------------------------------------------------------
            // STEP 1: AUTHENTICATION HANDSHAKE - REQUESTING THE BEARER TOKEN
            // ----------------------------------------------------------------
            System.out.println("\nExecuting Step 1: Querying Central Identity Provider...");

            // Simulating a standard client credential request payload
            String authPayload = "{\"clientId\":\"java_app_client\",\"clientSecret\":\"secret123\"}";

            HttpRequest authRequest = HttpRequest.newBuilder()
                    .uri(URI.create(authEndpoint))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(authPayload))
                    .build();

            HttpResponse<String> authResponse = client.send(authRequest, HttpResponse.BodyHandlers.ofString());

            if (authResponse.statusCode() != 200) {
                System.err.println("CRITICAL AUTH FAULT: Security handshake rejected by server.");
                return;
            }

            // Extract the secure cryptographic access token from the JSON string response
            JsonNode rootNode = jsonMapper.readTree(authResponse.body());
            String tokenValue = rootNode.get("access_token").asText();
            String tokenType = rootNode.get("token_type").asText();

            System.out.println("SUCCESS: Secure Authentication Token Acquired.");
            System.out.println("Extracted Key: " + tokenType + " " + tokenValue);

            // ----------------------------------------------------------------
            // STEP 2: DATA TRANSFORMATION & PROTECTED SHIPMENT
            // ----------------------------------------------------------------
            System.out.println("\nExecuting Step 2: Processing Business Payload Transformation...");

            // Create our target business data structure object instance
            SalesOrder secureOrder = new SalesOrder("ORD-2026-TOKEN-TEST", "Matharsa Cloud Solutions", 8950.75);

            // Transform the Java object into strict corporate XML layout strings
            XmlMapper xmlMapper = new XmlMapper();
            String transformedXml = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(secureOrder);

            System.out.println("--- Generated Protected XML Data Structure ---");
            System.out.println(transformedXml);
            System.out.println("----------------------------------------------");

            // Formulate our secure request containing our critical authentication token header
            HttpRequest secureDataRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ordersEndpoint))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/xml")
                    .header("Accept", "application/xml")
                    // Injecting the cryptographic bearer token to unlock the Mockoon firewall rule
                    .header("Authorization", tokenType + " " + tokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(transformedXml))
                    .build();

            System.out.println("\nShipping armored data packet directly through gateway firewall...");
            HttpResponse<String> orderResponse = client.send(secureDataRequest, HttpResponse.BodyHandlers.ofString());

            // ----------------------------------------------------------------
            // STEP 3: TRANSACTION EVALUATION TRACE
            // ----------------------------------------------------------------
            System.out.println("\n--- CONNECTIVITY METRICS ---");
            System.out.println("Gateway Protected Route Status Code: " + orderResponse.statusCode());
            System.out.println("----------------------------\n");

            if (orderResponse.statusCode() == 200) {
                System.out.println("PIPELINE TRIUMPH: Mockoon Gateway validated token successfully.");
                System.out.println("SAP Ledger Response Content:\n" + orderResponse.body());
            } else {
                System.out.println("PIPELINE BLOCKED: Gateway returned error message:\n" + orderResponse.body());
            }

        } catch (Exception e) {
            System.err.println("CRITICAL PIPELINE EXCEPTION: Security chain aborted: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n==================================================");
        System.out.println("PROCESS COMPLETED: Security trace execution closed.");
        System.out.println("==================================================");
    }
}
