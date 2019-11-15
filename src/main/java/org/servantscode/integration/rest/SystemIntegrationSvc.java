package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.integration.SystemIntegration;
import org.servantscode.integration.db.SystemIntegrationDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/systemIntegration")
public class SystemIntegrationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SystemIntegrationSvc.class);

    private SystemIntegrationDB db;

    public SystemIntegrationSvc() {
        db = new SystemIntegrationDB();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<SystemIntegration> getSystemIntegrations(@QueryParam("start") @DefaultValue("0") int start,
                                            @QueryParam("count") @DefaultValue("10") int count,
                                            @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                            @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("system.integration.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<SystemIntegration> results = db.getSystemIntegrations(nameSearch, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving systemIntegrations failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public SystemIntegration getSystemIntegration(@PathParam("id") int id) {
        verifyUserAccess("system.integration.read");
        try {
            return db.getSystemIntegration(id);
        } catch (Throwable t) {
            LOG.error("Retrieving system integration failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public SystemIntegration createSystemIntegration(SystemIntegration systemIntegration) {
        verifyUserAccess("system.integration.create");
        try {
            db.create(systemIntegration);
            LOG.info("Created system integration: " + systemIntegration.getName());
            return systemIntegration;
        } catch (Throwable t) {
            LOG.error("Creating system integration failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public SystemIntegration updateSystemIntegration(SystemIntegration systemIntegration) {
        verifyUserAccess("system.integration.update");
        try {
            db.updateSystemIntegration(systemIntegration);
            LOG.info("Edited system integration: " + systemIntegration.getName());
            return systemIntegration;
        } catch (Throwable t) {
            LOG.error("Updating system integration failed:", t);
            throw t;
        }
    }

    //To dangerous to compile in...

//    @DELETE @Path("/{id}")
//    public void deleteSystemIntegration(@PathParam("id") int id) {
//        verifyUserAccess("system.integration.delete");
//        if(id <= 0)
//            throw new NotFoundException();
//        try {
//            SystemIntegration systemIntegration = db.getSystemIntegration(id);
//            if(systemIntegration == null || !db.deleteSystemIntegration(id))
//                throw new NotFoundException();
//            LOG.info("Deleted system integration: " + systemIntegration.getName());
//        } catch (Throwable t) {
//            LOG.error("Deleting systemIntegration failed:", t);
//            throw t;
//        }
//    }
}
