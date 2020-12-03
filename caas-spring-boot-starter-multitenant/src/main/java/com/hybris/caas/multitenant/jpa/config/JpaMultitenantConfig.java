package com.hybris.caas.multitenant.jpa.config;

import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Provides configuration for JPA (EclipseLink) multitenant support.
 */
@EnableTransactionManagement(order = 100)
public class JpaMultitenantConfig
{
}
