package org.servantscode.integration.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;

@Path("/")
public class HelloSvc {
    private static final Logger LOG = LogManager.getLogger(HelloSvc.class);

    @GET 
    public String integration() {
        LOG.trace("Hello world! Logging is working");
        return "Hello World! Servant's Code is here!";
    }
}
