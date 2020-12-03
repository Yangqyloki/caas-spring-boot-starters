package com.hybris.caas.multitenant;

import static com.hybris.caas.multitenant.Constants.TENANT_PATTERN;

public class TenantUtils
{
	private TenantUtils()
	{
		// making sonar happy
	}

	public static boolean isValid(final String tenantId)
	{
		return tenantId != null && TENANT_PATTERN.matcher(tenantId).matches();
	}

}
