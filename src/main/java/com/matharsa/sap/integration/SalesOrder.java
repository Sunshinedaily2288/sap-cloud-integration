package com.matharsa.sap.integration;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// This annotation defines what the outer XML tag will be named
@JacksonXmlRootElement(localName = "PurchaseOrder")
public class SalesOrder {

    // These annotations map our standard Java variables to specific strict XML tags
    @JacksonXmlProperty(localName = "ID")
    private String orderId;

    @JacksonXmlProperty(localName = "ClientName")
    private String customer;

    @JacksonXmlProperty(localName = "TotalCurrencyValue")
    private double amount;

    // Default constructor required by the Jackson parsing engine
    public SalesOrder() {}

    // Main constructor to populate data elements easily
    public SalesOrder(String orderId, String customer, double amount) {
        this.orderId = orderId;
        this.customer = customer;
        this.amount = amount;
    }

    // Getters and Setters so our application can access the data variables
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
