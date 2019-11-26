package org.servantscode.integration.rest;

public class CreateAccountRequest {
    private int donorId;
    private int personId;
    private int familyId;

    // ----- Accessors -----
    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }

    public int getFamilyId() { return familyId; }
    public void setFamilyId(int familyId) { this.familyId = familyId; }
}
