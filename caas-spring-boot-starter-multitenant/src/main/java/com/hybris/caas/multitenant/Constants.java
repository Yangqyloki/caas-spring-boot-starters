package com.hybris.caas.multitenant;

import com.hybris.caas.multitenant.filter.TenantHeaderFilter;

import java.util.regex.Pattern;

/**
 * Defines constants used by caas-spring-boot-starter-multitenant.
 */
public class Constants
{
	/**
	 * Attribute name used to store the tenant extracted from a request.
	 * For implementation details see {@link TenantHeaderFilter}
	 */
	public static final String TENANT_ATTRIBUTE_NAME = "com.hybris.caas.multitenant.tenant";
	public static final String TENANT = "Tenant";

	/**
	 * regex pattern to validate tenant identifiers
	 */
	public static final Pattern TENANT_PATTERN = Pattern.compile("[a-zA-Z0-9.-]+$");

	private Constants()
	{
		// private constructor
	}
}
