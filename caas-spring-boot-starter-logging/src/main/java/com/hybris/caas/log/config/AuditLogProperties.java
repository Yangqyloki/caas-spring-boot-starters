package com.hybris.caas.log.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Defines audit logging properties.
 * <p>
 * Extracts the service name from spring.application.name property.
 */
@ConfigurationProperties(prefix = "sap.audit.service")
public class AuditLogProperties {
    @Value("${spring.application.name:-}")
    private String serviceName;
    private boolean enabled;
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }
}
