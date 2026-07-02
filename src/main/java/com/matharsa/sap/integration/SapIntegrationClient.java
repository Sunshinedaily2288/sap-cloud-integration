package com.matharsa.sap.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class SapIntegrationClient {

    public static void main(String[] args) {
        // Updated stable endpoint path
        String sapEndpoint = "https://odata.org";

        System.out.println("==================================================");
        System.out.println("INITIALIZING ROUTE: Java Cloud Layer -> SAP API Gateway");
        System.out.println("==================================================");

        try {
            // 1. Create a Development-only Trust Manager to handle the server's SSL mismatch
            TrustManager[] devTrustManager = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            // 2. Initialize an accommodating SSL context for the pipeline
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, devTrustManager, new java.security.SecureRandom());

            // Disable strict hostname verification for this execution path
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

            // 3. Build the resilient HTTP Client with custom SSL Context and Redirect Policy
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .sslContext(sslContext) // Inject custom SSL rules
                    .followRedirects(HttpClient.Redirect.ALWAYS) // <-- ADD THIS LINE HERE
                    .build();


            // 4. Formulate the HTTP GET request accepting any native enterprise layout
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sapEndpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "*/*") // <-- CHANGE THIS FROM "application/json" TO "*/*"
                    .GET()
                    .build();


            System.out.println("Sending encrypted HTTPS request packet through SSL tunnel...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("\n--- CONNECTIVITY METRICS ---");
            System.out.println("HTTP Response Status Code: " + response.statusCode());
            System.out.println("----------------------------\n");

            if (response.statusCode() == 200) {
                System.out.println("SUCCESS: Live Data Tunnel Active.");
                System.out.println("SAP Metadata Payload Received:");

                String responseBody = response.body();
                if (responseBody.length() > 600) {
                    System.out.println(responseBody.substring(0, 600) + "\n... [Truncated]");
                } else {
                    System.out.println(responseBody);
                }
            } else {
                System.out.println("FAILURE: Remote server returned code: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("CRITICAL FAULT: Pipeline integration failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n==================================================");
        System.out.println("PROCESS COMPLETED: Execution trace finalized.");
        System.out.println("==================================================");
    }
}
