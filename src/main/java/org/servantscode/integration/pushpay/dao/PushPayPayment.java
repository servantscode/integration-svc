package org.servantscode.integration.pushpay.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Map;

public class PushPayPayment {
    private String status;
    private String transactionId;
    private String paymentToken;
    private PushPayAmount amount;
    private PushPayPayer payer;
    private PushPayRecipient recipient;
    private ZonedDateTime createdOn;
    private ZonedDateTime updatedOn;
    private String paymentMethodType;
    private String source;
    private String ipAddress;
    private PushPayFund fund;
    @JsonProperty("_links")
    private Map<String, PushPayLink> links;
//    "fields": [
//        {
//            "key": "1",
//            "value": "For last week",
//            "label": "Message"
//        },
//        {
//            "key": "2",
//            "value": "123",
//            "label": "Campus"
//        }
//    ],
//    "card": {
//        "reference": "4111-11....1111",
//        "brand": "VISA",
//        "logo": "https://pushpay.com/Content/PushpayWeb/Images/Interface/@2x/PaymentMethods/visa.png"
//    },
//    "campus": {
//        "name": "Eastbrook Campus",
//        "key": "MTY0NTgwOlk4ZnY4ZDUzWE1mOGt2RGxXTEM4MzdaTUUxTQ"
//    }

    // ----- Accessors -----
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = "PushPay:" + transactionId; }

    public String getPaymentToken() { return paymentToken; }
    public void setPaymentToken(String paymentToken) { this.paymentToken = paymentToken; }

    public PushPayAmount getAmount() { return amount; }
    public void setAmount(PushPayAmount amount) { this.amount = amount; }

    public PushPayPayer getPayer() { return payer; }
    public void setPayer(PushPayPayer payer) { this.payer = payer; }

    public PushPayRecipient getRecipient() { return recipient; }
    public void setRecipient(PushPayRecipient recipient) { this.recipient = recipient; }

    public ZonedDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(ZonedDateTime createdOn) { this.createdOn = createdOn; }

    public ZonedDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(ZonedDateTime updatedOn) { this.updatedOn = updatedOn; }

    public String getPaymentMethodType() { return paymentMethodType; }
    public void setPaymentMethodType(String paymentMethodType) { this.paymentMethodType = paymentMethodType; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public PushPayFund getFund() { return fund; }
    public void setFund(PushPayFund fund) { this.fund = fund; }

    public Map<String, PushPayLink> getLinks() { return links; }
    public void setLinks(Map<String, PushPayLink> links) { this.links = links; }
}
