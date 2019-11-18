package org.servantscode.integration.pushpay.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Map;

public class PushPayPayer {
    private String key;
    private String emailAddress;
    private boolean emailAddressVerified;
    private String mobileNumber;
    private boolean mobileNumberVerified;
    private String fullName;
    private String firstName;
    private String lastName;
    private PushPayAddress address;
    private ZonedDateTime updatedOn;
    private String role;
    private String payerType;
    @JsonProperty("_links")
    private Map<String, PushPayLink> links;

    // ----- Accessors -----
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public boolean isEmailAddressVerified() { return emailAddressVerified; }
    public void setEmailAddressVerified(boolean emailAddressVerified) { this.emailAddressVerified = emailAddressVerified; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public boolean isMobileNumberVerified() { return mobileNumberVerified; }
    public void setMobileNumberVerified(boolean mobileNumberVerified) { this.mobileNumberVerified = mobileNumberVerified; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public PushPayAddress getAddress() { return address; }
    public void setAddress(PushPayAddress address) { this.address = address; }

    public ZonedDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(ZonedDateTime updatedOn) { this.updatedOn = updatedOn; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPayerType() { return payerType; }
    public void setPayerType(String payerType) { this.payerType = payerType; }

    public Map<String, PushPayLink> getLinks() { return links; }
    public void setLinks(Map<String, PushPayLink> links) { this.links = links; }
}
