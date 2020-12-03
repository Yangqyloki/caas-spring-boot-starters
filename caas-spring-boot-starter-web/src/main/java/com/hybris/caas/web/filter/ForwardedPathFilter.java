package com.hybris.caas.web.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Checks for the header {@code X-Forwarded-Path}, given by the approuter. Based on the current URL path, creates the header
 * {@code X-Forwarded-Prefix}, understood by {@link ForwardedHeaderFilter}, in order to build the links.
 * Note: Needs to run before {@link ForwardedHeaderFilter}
 * For example:
 * For the given {@code X-Forwarded-Path: /public/product-content/products}
 * A header {@code X-Forwarded-Prefix: /public/product-content} is created
 */
@Order(CaasForwardedHeaderFilter.FORWARDED_HEADER_ORDER - 50) // needs to run before ForwardedHeaderFilter
@Component
@ConditionalOnProperty(prefix = "server", name = "use-forward-prefix", matchIfMissing = true)
public class ForwardedPathFilter extends OncePerRequestFilter
{

	public static final String X_FORWARDED_PATH = "X-Forwarded-Path";
	public static final String X_FORWARDED_PREFIX = "X-Forwarded-Prefix";
	public static final Pattern JOINED_SLASHES = Pattern.compile("//+");

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException
	{
		final String forwardedPath = request.getHeader(X_FORWARDED_PATH);
		final String forwardedPrefix = request.getHeader(X_FORWARDED_PREFIX);

		if (forwardedPath != null && forwardedPrefix == null)
		{
			String requestURI = request.getServletPath();
			if (requestURI.endsWith("/"))
			{
				requestURI = requestURI.substring(0, requestURI.length() - 1);
			}
			// spring cleans up the servlet path (requestURI) to have only single slashes path separators
			// in case X-Forwarded-Path has multiple slashes together, clean them up to have single path separators
			final String calculatedForwardedPrefix = JOINED_SLASHES.matcher(forwardedPath).replaceAll("/").replace(requestURI, "");

			final HeaderModifiableRequest headerModifiableRequest = new HeaderModifiableRequest(request);
			headerModifiableRequest.addHeader(X_FORWARDED_PREFIX, calculatedForwardedPrefix);

			filterChain.doFilter(headerModifiableRequest, response);
		}
		else
		{
			filterChain.doFilter(request, response);
		}
	}

}
