package com.hybris.caas.multitenant.service;

import com.hybris.caas.multitenant.service.config.TenantProperties;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import com.hybris.caas.multitenant.service.exception.MissingTenantException.AccessType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

import static com.hybris.caas.multitenant.Constants.TENANT_ATTRIBUTE_NAME;

/**
 * This implementation of the <code>TenantService</code> will extract the tenant
 * from a JWT token using the SAP Java container library if available, otherwise from X-Forwarded-Host header.
 */
public class SapJwtTenantService implements TenantService
{
	private final TenantProperties tenantProperties;

	/**
	 * Class constructor.
	 *
	 * @param tenantProperties to keep a reference to the tenant properties
	 */
	public SapJwtTenantService(final TenantProperties tenantProperties)
	{
		this.tenantProperties = tenantProperties;
	}

	@Override
	public String getTenant()
	{
		return getTenant(getRequest());
	}

	@Override
	public String getTenant(HttpServletRequest request)
	{
		final String tenantAttribValue = (String) request.getAttribute(TENANT_ATTRIBUTE_NAME);
		final Optional<String> tenant = Optional.ofNullable(tenantAttribValue);

		return tenant.orElseThrow(() -> missingTenantException(request.getHeader(HttpHeaders.AUTHORIZATION)));
	}

	private MissingTenantException missingTenantException(final String authHeader)
	{
		return new MissingTenantException(Optional.ofNullable(authHeader).map(h -> AccessType.PROTECTED).orElse(AccessType.PUBLIC));
	}

	@Override
	public boolean isPaasTenant()
	{
		return Objects.nonNull(tenantProperties.getPaasName()) && tenantProperties.getPaasName().equals(getTenant());
	}

	HttpServletRequest getRequest()
	{
		final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes == null)
		{
			throw new IllegalArgumentException();
		}
		return ((ServletRequestAttributes) attributes).getRequest();
	}

}
