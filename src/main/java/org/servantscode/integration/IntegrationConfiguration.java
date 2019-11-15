package org.servantscode.integration;

import java.util.Map;

public abstract class IntegrationConfiguration {

    public abstract void setConfiguration(Map<String, String> configuration);
    public abstract Map<String, String> toMap();
}
