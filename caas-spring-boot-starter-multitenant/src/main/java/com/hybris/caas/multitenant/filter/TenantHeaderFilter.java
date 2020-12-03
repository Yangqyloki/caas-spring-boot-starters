package com.hybris.caas.multitenant.filter;

import com.hybris.caas.multitenant.Constants;
import com.hybris.caas.multitenant.TenantUtils;
import com.hybris.caas.multitenant.service.TokenProvider;
import com.hybris.caas.multitenant.service.config.TenantProperties;
import com.hybris.caas.multitenant.service.exception.InvalidTenantException;
import com.hybris.caas.multitenant.service.exception.InvalidTenantFormatException;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static com.hybris.caas.multitenant.Constants.TENANT_ATTRIBUTE_NAME;

/**
 * Filter that extracts the tenant name from the request, if available. Stores the tenant name in the request as an attribute with the
 * key {@link Constants#TENANT_ATTRIBUTE_NAME}.
 * NOTE: Needs to run before {@link ForwardedHeaderFilter} which removes the {@code X-Forwarded-Host} header.
 * A {@link ForwardedHeaderFilter} is configured in {@code com.hybris.caas.web.config.WebConfig#forwardedHeaderFilter()}, see its
 * implementation to check the filter order. This filter needs to run before the order configured in
 * {@code com.hybris.caas.web.filter.CaasForwardedHeaderFilter#FORWARDED_HEADER_ORDER}
 */
@Order(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 60)
@Component
public class TenantHeaderFilter extends OncePerRequestFilter
{
	static final String X_FORWARDED_HOST = "X-Forwarded-Host";
	static final String BEARER = "bearer";

	private final TenantProperties tenantProperties;
	private final TokenProvider tokenProvider;

	public TenantHeaderFilter(final TenantProperties tenantProperties, final TokenProvider tokenProvider)
	{
		this.tenantProperties = tenantProperties;
		this.tokenProvider = tokenProvider;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException
	{
		final Optional<String> tenant = getTenant(request);

		tenant.ifPresent(t -> request.setAttribute(TENANT_ATTRIBUTE_NAME, t));

		filterChain.doFilter(request, response);
	}

	/**
	 * Cases covered:
	 * <ul>
	 * <li>when the {@code Authorization} header contains a JWT token, extracts the tenant from it.</li>
	 * <li>when no {@code Authorization} header present, extracts the tenant from the {@code X-Forwarded-Host} header via the configured regexp matching group</li>
	 * <li>when both {@code Authorization} and {@code X-Forwarded-Host} headers are present, ensures same tenant is used in both.</li>
	 * </ul>
	 *
	 * @param request the http servlet request
	 * @return the tenant name if available in the request otherwise empty optional
	 */
	private Optional<String> getTenant(final HttpServletRequest request)
	{
		// Get tenant from request using tenant properties
		final TenantProperties.TenantPathProperties pathProperties = tenantProperties.getPropertiesForPath(request.getServletPath());
		final Optional<String> tenantFromHeader = extractTenantFromRequest(request, pathProperties).map(
				tenant -> tenant.toLowerCase(Locale.ENGLISH));

		// If Authorization header present and value starts with "Bearer", attempt to get the tenant from the already parsed JWT token.
		// Spring allows "bearer" to be case insensitive
		final Optional<String> oAuth2 = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
				.map(authHeader -> authHeader.toLowerCase(Locale.ENGLISH))
				.filter(authHeader -> authHeader.startsWith(BEARER));

		if (oAuth2.isPresent())
		{
			final String tenantFromJwt;
			try
			{
				tenantFromJwt = tokenProvider.getToken().getSubdomain().toLowerCase(Locale.ENGLISH);
			}
			catch (final Exception e)
			{
				throw new MissingTenantException(MissingTenantException.AccessType.PROTECTED, e);
			}

			// validate that the tenant from the header and the tenant from the token are the same when they are both provided
			tenantFromHeader.filter(tenant -> !tenant.equals(tenantFromJwt)).ifPresent(tenant -> {
				throw new InvalidTenantException();
			});

			return Optional.of(tenantFromJwt);
		}
		else
		{
			return tenantFromHeader;
		}
	}

	private Optional<String> extractTenantFromRequest(final HttpServletRequest request,
			final TenantProperties.TenantPathProperties pathProperties)
	{
		Optional<String> tenant = Optional.ofNullable(request.getHeader(X_FORWARDED_HOST))
				.filter(host -> !StringUtils.isEmpty(host))
				.flatMap(pathProperties::getTenantFromHost);
		if (tenant.isPresent() && !TenantUtils.isValid(tenant.get()))
		{
			throw new InvalidTenantFormatException();
		}
		return tenant;
	}

}
