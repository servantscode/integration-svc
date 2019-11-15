package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.integration.AvailableIntegration;
import org.servantscode.integration.OrganizationIntegration;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.OrganizationIntegrationDB;
import org.servantscode.integration.db.SystemIntegrationDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

import static org.servantscode.commons.StringUtils.isSet;

@Path("/integration")
public class OrganizationIntegrationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(OrganizationIntegrationSvc.class);

    private OrganizationIntegrationDB db;
    private SystemIntegrationDB systemIntDb;

    public OrganizationIntegrationSvc() {
        db = new OrganizationIntegrationDB();
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
            List<OrganizationIntegration> currentIntegrations = db.getOrganizationIntegrations("", "si.name", 0, 0);
            List<AvailableIntegration> available = results.stream()
                    .filter(i -> currentIntegrations.stream().noneMatch(c -> c.getSystemIntegrationId() == i.getId()))
                    .map(this::toAvailable).collect(Collectors.toList());

            return new PaginatedResponse<>(start, results.size(), totalAvailable, available);
        } catch (Throwable t) {
            LOG.error("Retrieving organizationIntegrations failed:", t);
            throw t;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<OrganizationIntegration> getOrganizationIntegrations(@QueryParam("start") @DefaultValue("0") int start,
                                            @QueryParam("count") @DefaultValue("10") int count,
                                            @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                            @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("admin.integration.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<OrganizationIntegration> results = db.getOrganizationIntegrations(nameSearch, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving organizationIntegrations failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public OrganizationIntegration getOrganizationIntegration(@PathParam("id") int id) {
        verifyUserAccess("admin.integration.read");
        try {
            return db.getOrganizationIntegration(id);
        } catch (Throwable t) {
            LOG.error("Retrieving organization integration failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public OrganizationIntegration createOrganizationIntegration(OrganizationIntegration organizationIntegration) {
        verifyUserAccess("admin.integration.create");
        try {
            db.create(organizationIntegration);
            LOG.info("Created organization integration: " + organizationIntegration.getName());
            return organizationIntegration;
        } catch (Throwable t) {
            LOG.error("Creating organization integration failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public OrganizationIntegration updateOrganizationIntegration(OrganizationIntegration organizationIntegration) {
        verifyUserAccess("admin.integration.update");
        try {
            db.updateOrganizationIntegration(organizationIntegration);
            LOG.info("Edited organization integration: " + organizationIntegration.getName());
            return organizationIntegration;
        } catch (Throwable t) {
            LOG.error("Updating organization integration failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteOrganizationIntegration(@PathParam("id") int id) {
        verifyUserAccess("admin.integration.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            OrganizationIntegration organizationIntegration = db.getOrganizationIntegration(id);
            if(organizationIntegration == null || !db.deleteOrganizationIntegration(id))
                throw new NotFoundException();
            LOG.info("Deleted organization integration: " + organizationIntegration.getName());
        } catch (Throwable t) {
            LOG.error("Deleting organizationIntegration failed:", t);
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
