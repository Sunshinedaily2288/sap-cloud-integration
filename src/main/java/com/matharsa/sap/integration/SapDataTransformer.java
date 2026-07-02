package com.matharsa.sap.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SapDataTransformer {

    public static void main(String[] args) {
        // Our running Mockoon Gateway URL destination
        String mockoonEndpoint = "http://localhost:8097/api/sap/orders";

        // Simulating an incoming clean JSON payload from an AWS hosted storefront
        String inboundJsonPayload = """
        {
          "orderId": "ORD-2026-99",
          "customer": "Matharsa Enterprise",
          "amount": 4500.00
        }
        """;

        System.out.println("==================================================");
        System.out.println("LAUNCHING ENTERPRISE PIPELINE: JSON -> XML Transformation");
        System.out.println("==================================================");

        try {
            // 1. Parse the inbound JSON string into our structured SalesOrder object
            ObjectMapper jsonMapper = new ObjectMapper();
            SalesOrder order = jsonMapper.readValue(inboundJsonPayload, SalesOrder.class);
            System.out.println("1. Inbound JSON safely parsed into local Java data object model.");

            // 2. Transform the Java object into strict corporate XML formatting
            XmlMapper xmlMapper = new XmlMapper();
            String outboundXmlPayload = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(order);

            System.out.println("\n2. SUCCESS: Structural Transformation Complete.");
            System.out.println("--- Generated Outbound XML Packet Stream ---");
            System.out.println(outboundXmlPayload);
            System.out.println("--------------------------------------------\n");

            // 3. Build a standard native HTTP Client to ship our payload
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // 4. Formulate an HTTP POST request sending our XML payload with explicit content headers
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mockoonEndpoint))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/xml")
                    .header("Accept", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(outboundXmlPayload))
                    .build();

            System.out.println("3. Dispatching data packet to Mockoon SAP Gateway on Port 8097...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 5. Evaluate the return transaction state
            System.out.println("\n--- CONNECTIVITY METRICS ---");
            System.out.println("Gateway HTTP Response Status Code: " + response.statusCode());
            System.out.println("----------------------------\n");

            if (response.statusCode() == 200) {
                System.out.println("4. PIPELINE SUCCESS: Response received from SAP core proxy system:");
                System.out.println(response.body());
            } else {
                System.out.println("4. PIPELINE ALERT: Server rejected packet with status code: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE: Data pipeline transformation aborted: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n==================================================");
        System.out.println("PROCESS COMPLETED: Execution trace finalized.");
        System.out.println("==================================================");
    }
}
