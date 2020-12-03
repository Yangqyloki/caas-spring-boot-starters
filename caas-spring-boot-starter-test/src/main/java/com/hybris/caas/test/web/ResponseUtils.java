package com.hybris.caas.test.web;

import io.restassured.response.Response;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Utility class exposing static helper functions for HTTP responses.
 */
public final class ResponseUtils
{
	private static final String ERROR = "location header not present in response";

	private ResponseUtils()
	{
		// private constructor
	}

	/**
	 * Get the final path parameter from the response's <code>Location</code>
	 * header.
	 *
	 * @param response
	 * @return the id of a newly created resource
	 */
	public static String getIdFromLocationHeader(final Response response)
	{
		final String location = response.getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1, location.length());
	}

	/**
	 * Get the final path parameter from the response's <code>Location</code> header.
	 *
	 * @return the id of a newly created resource
	 */
	public static Function<Response, String> getIdFromLocationHeader()
	{
		return response -> {
			final String location = response.getHeader(HttpHeaders.LOCATION);
			Assert.notNull(location, ERROR);
			return FilenameUtils.getName(location);
		};
	}

}
