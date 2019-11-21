package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.AvailableIntegration;
import org.servantscode.integration.Integration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.IntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

import static org.servantscode.commons.StringUtils.isSet;

@Path("/integration")
public class IntegrationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(IntegrationSvc.class);

    private IntegrationDB db;
    private SystemIntegrationDB systemIntDb;

    public IntegrationSvc() {
        db = new IntegrationDB();
        systemIntDb = new SystemIntegrationDB();
    }

    @GET @Path("/available") @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<AvailableIntegration> getAvailableIntegrations(@QueryParam("start") @DefaultValue("0") int start,
                                                                            @QueryParam("count") @DefaultValue("10") int count,
                                                                            @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                                            @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("admin.integration.list");
        try {
            int totalAvailable = systemIntDb.getCount(nameSearch);

            List<SystemIntegration> results = systemIntDb.getSystemIntegrations(nameSearch, sortField, start, count);
            List<Integration> currentIntegrations = db.getIntegrations("", "si.name", 0, 0);
            List<AvailableIntegration> available = results.stream()
                    .filter(i -> currentIntegrations.stream().noneMatch(c -> c.getSystemIntegrationId() == i.getId()))
                    .map(this::toAvailable).collect(Collectors.toList());

            return new PaginatedResponse<>(start, results.size(), totalAvailable, available);
        } catch (Throwable t) {
            LOG.error("Retrieving integrations failed:", t);
            throw t;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Integration> getIntegrations(@QueryParam("start") @DefaultValue("0") int start,
                                                                      @QueryParam("count") @DefaultValue("10") int count,
                                                                      @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                                      @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("admin.integration.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<Integration> results = db.getIntegrations(nameSearch, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving integrations failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Integration getIntegration(@PathParam("id") int id) {
        verifyUserAccess("admin.integration.read");
        try {
            return db.getIntegration(id);
        } catch (Throwable t) {
            LOG.error("Retrieving integration failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Integration createIntegration(Integration integration) {
        verifyUserAccess("admin.integration.create");
        try {
            db.create(integration);
            LOG.info("Created integration: " + integration.getName());
            return integration;
        } catch (Throwable t) {
            LOG.error("Creating integration failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Integration updateIntegration(Integration integration) {
        verifyUserAccess("admin.integration.update");
        try {
            Integration existing = db.getIntegration(integration.getId());
            integration.setConfig(existing.getConfig());
            db.update(integration);
            LOG.info("Edited integration: " + integration.getName());
            return integration;
        } catch (Throwable t) {
            LOG.error("Updating integration failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteIntegration(@PathParam("id") int id) {
        verifyUserAccess("admin.integration.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Integration integration = db.getIntegration(id);
            if(integration == null || !db.deleteIntegration(id))
                throw new NotFoundException();
            LOG.info("Deleted  integration: " + integration.getName());
        } catch (Throwable t) {
            LOG.error("Deleting integration failed:", t);
            throw t;
        }
    }

    // ----- Private -----
    private AvailableIntegration toAvailable(SystemIntegration sysInt) {
        AvailableIntegration aInt = new AvailableIntegration();
        aInt.setId(sysInt.getId());
        aInt.setName(sysInt.getName());

        String authUrl = sysInt.getConfig().get("authUrl");
        if(isSet(authUrl))
            aInt.setAuthorizationUrl(customizeAuthUrl(authUrl));
        return aInt;
    }

    private String customizeAuthUrl(String authUrl) {
        String orgName = OrganizationContext.getOrganization().getHostName();
        return authUrl.replace(":orgName:", orgName);
    }
}
