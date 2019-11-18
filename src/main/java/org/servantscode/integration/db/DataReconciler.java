package org.servantscode.integration.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Map;

public class DataReconciler extends DBAccess {
    private int orgId;

    public DataReconciler(int orgId) {
        this.orgId = orgId;
    }

    public int getFundIdForName(String fundName) {
        QueryBuilder query = select("id").from("funds").with("name", fundName).inOrg(orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn);
            ResultSet rs = stmt.executeQuery()) {

            return rs.next()? rs.getInt(1): 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not query funds: " + e.getMessage(), e);
        }
    }

    public int createFund(String fundName) {
        InsertBuilder cmd = insertInto("funds").value("name", fundName).value("org_id", orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = cmd.prepareStatement(conn, true)) {

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create fund record.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next())
                    throw new RuntimeException("No new key generated for created fund.");

                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not query funds: " + e.getMessage(), e);
        }
    }

    public int getFamilyId(String personName, String personEmail, String personPhone) {
        QueryBuilder phoneQuery = select("family_id").from("person_phone_numbers pn").leftJoin("people p ON pn.person_id=p.id").with("number", personPhone);
        try(Connection conn = getConnection();
            PreparedStatement stmt = phoneQuery.prepareStatement(conn);
            ResultSet rs = stmt.executeQuery()) {

            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not query people by phone number: " + e.getMessage(), e);
        }

        QueryBuilder emailQuery = select("family_id").from("people").with("email", personEmail).inOrg(orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = emailQuery.prepareStatement(conn);
            ResultSet rs = stmt.executeQuery()) {

            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not query people by email address: " + e.getMessage(), e);
        }

        QueryBuilder query = select("family_id").from("people").with("name", personName).inOrg(orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn);
            ResultSet rs = stmt.executeQuery()) {

            int personId = 0;
            if(rs.next())
                personId = rs.getInt(1);

            return rs.next()? 0: personId; //If there is more than 1 name match, do not presume that any of them are the correct one.
        } catch (SQLException e) {
            throw new RuntimeException("Could not query funds: " + e.getMessage(), e);
        }
    }

    public int createPerson(String personName, String personEmail, String personPhone) {
        int familyId = 0;

        String[] splitName = personName.split(" ");
        InsertBuilder familyCmd = insertInto("families").value("surname", splitName[splitName.length-1]).value("org_id", orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = familyCmd.prepareStatement(conn, true)) {

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create person record.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next())
                    throw new RuntimeException("No new key generated for created family.");

                familyId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create family: " + e.getMessage(), e);
        }

        int personId = 0;

        InsertBuilder personCmd = insertInto("people").value("name", personName)
                .value("family_id", familyId).value("email", personEmail).value("org_id", orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = personCmd.prepareStatement(conn, true)) {

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create person record.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next())
                    throw new RuntimeException("No new key generated for created person.");

                personId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create person: " + e.getMessage(), e);
        }

        InsertBuilder phoneCmd = insertInto("person_phone_numbers")
                .value("person_id", personId)
                .value("number", personPhone)
                .value("type", "CELL")
                .value("is_primary", true);
        try(Connection conn = getConnection();
            PreparedStatement stmt = phoneCmd.prepareStatement(conn)) {

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create phone record.");
        } catch (SQLException e) {
            throw new RuntimeException("Could not create phone number: " + e.getMessage(), e);
        }
        return familyId;
    }


    public void createDonation(Map<String, Object> donation) {
        InsertBuilder cmd = insertInto("donations")
                .value("family_id", donation.get("familyId"))
                .value("fund_id", donation.get("fundId"))
//                .value("pledge_id", donation.getPledgeId())
                .value("amount", donation.get("amount"))
                .value("deductible_amount", donation.get("deductibleAmount"))
                .value("date", donation.get("donationDate"))
                .value("type", donation.get("donationType"))
                .value("transaction_id", donation.get("transactionId"))
//                .value("batch_number", donation.getBatchNumber())
//                .value("notes", donation.getNotes())
//                .value("recorded_time", ZonedDateTime.now())
//                .value("recorder_id", 0)
                .value("org_id", orgId);

        try(Connection conn = getConnection();
            PreparedStatement stmt = cmd.prepareStatement(conn)) {

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create donation.");
        } catch (SQLException e) {
            throw new RuntimeException("Could not create donation: " + e.getMessage(), e);
        }
    }

    public int getPledgeId(int familyId, int fundId) {
        QueryBuilder query = select("id").from("pledges").with("family_id", familyId).with("fund_id", fundId).inOrg(orgId);
        try(Connection conn = getConnection();
            PreparedStatement stmt = query.prepareStatement(conn);
            ResultSet rs = stmt.executeQuery()) {

            return rs.next()? rs.getInt(0): 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not query pledges: " + e.getMessage(), e);
        }
    }
}
