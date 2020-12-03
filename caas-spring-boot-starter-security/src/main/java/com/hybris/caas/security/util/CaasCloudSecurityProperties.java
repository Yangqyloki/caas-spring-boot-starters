package com.hybris.caas.security.util;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Encapsulates configuration properties required for securing the service.
 */
@Validated
public class CaasCloudSecurityProperties
{
	public CaasCloudSecurityProperties()
	{
		//empty
	}

	public CaasCloudSecurityProperties(@NotEmpty final String xsappname, @NotEmpty final String tenantRegex,
			@NotEmpty final String manageScope)
	{
		this.xsappname = xsappname;
		this.tenantRegex = tenantRegex;
		this.manageScope = manageScope;
	}

	@NotEmpty
	private String xsappname;
	@NotEmpty
	private String tenantRegex;
	@NotEmpty
	private String manageScope;

	public String getXsappname()
	{
		return xsappname;
	}

	public void setXsappname(final String xsappname)
	{
		this.xsappname = xsappname;
	}

	public String getTenantRegex()
	{
		return tenantRegex;
	}

	public void setTenantRegex(final String tenantRegex)
	{
		this.tenantRegex = tenantRegex;
	}

	public String getManageScope()
	{
		return manageScope;
	}

	public void setManageScope(final String manageScope)
	{
		this.manageScope = manageScope;
	}
}
