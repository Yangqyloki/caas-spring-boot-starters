package com.hybris.caas.test.integration.util;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Once per request servlet filter to throw an exception for requests with path info {@code /fail}.
 */
public class FailureInducingFilter extends OncePerRequestFilter
{
	private static final String FAILURE_PATH = "/fail";
	private static final String FAILURE_LOCATION = "location";
	private static final String FAILURE_LOCATION_AFTER = "after";

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException
	{
		if (FAILURE_PATH.equals(request.getRequestURI()) && !FAILURE_LOCATION_AFTER.equalsIgnoreCase(request.getParameter(FAILURE_LOCATION)))
		{
			throw new IllegalArgumentException("Failing request on purpose BEFORE the filter chain.");
		}

		filterChain.doFilter(request, response);

		if (FAILURE_PATH.equals(request.getRequestURI()) && FAILURE_LOCATION_AFTER.equalsIgnoreCase(request.getParameter(FAILURE_LOCATION)))
		{
			throw new IllegalArgumentException("Failing request on purpose AFTER the filter chain.");
		}
	}
}
