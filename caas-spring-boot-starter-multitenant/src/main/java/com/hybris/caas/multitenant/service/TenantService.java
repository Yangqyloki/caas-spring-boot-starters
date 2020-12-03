package com.hybris.caas.multitenant.service;

import javax.servlet.http.HttpServletRequest;

import com.hybris.caas.multitenant.service.exception.MissingTenantException;

/**
 * This service is a meant to be used as a strategy for getting the tenant for a given HTTP request.
 */
public interface TenantService
{
	/**
	 * Get the active tenant.
	 *
	 * @return the tenant in lower case
	 * @throws MissingTenantException when the tenant could not be retrieved
	 */
	String getTenant();

	/**
	 * Get the active tenant from a given HTTP request
	 *
	 * @param request the HTTP request
	 * @return the tenant in lower case
	 * @throws MissingTenantException when the tenant could not be retrieved
	 */
	String getTenant(HttpServletRequest request);

	/**
	 * Checks if the active tenant is the configured PaaS tenant.
	 *
	 * @return true if the active tenant is the configured PaaS tenant, otherwise false
	 */
	boolean isPaasTenant();
}
