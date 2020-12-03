package com.hybris.caas.web.filter;

import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An http request that can have http headers added
 */
public class HeaderModifiableRequest extends HttpServletRequestWrapper
{
	private final Map<String, List<String>> headers;

	public HeaderModifiableRequest(HttpServletRequest request)
	{
		super(request);
		this.headers = initHeaders(request);
	}

	private static Map<String, List<String>> initHeaders(HttpServletRequest request)
	{
		final Map<String, List<String>> headers = new LinkedCaseInsensitiveMap<>(Locale.ENGLISH);
		final Enumeration<String> names = request.getHeaderNames();

		while (names.hasMoreElements())
		{
			final String name = names.nextElement();
			headers.put(name, Collections.list(request.getHeaders(name)));
		}
		return headers;
	}

	public void addHeader(String name, String value)
	{
		final List<String> values = headers.get(name);
		if (values == null)
		{
			headers.put(name, List.of(value));
		}
		else
		{
			values.add(value);
		}
	}

	public void setHeader(String name, String value)
	{
		headers.put(name, List.of(value));
	}

	@Override
	public String getHeader(String name)
	{
		final List<String> value = headers.get(name);
		return (CollectionUtils.isEmpty(value) ? null : value.get(0));
	}

	@Override
	public Enumeration<String> getHeaders(String name)
	{
		final List<String> value = headers.get(name);
		return (Collections.enumeration(value != null ? value : Collections.emptySet()));
	}

	@Override
	public Enumeration<String> getHeaderNames()
	{
		return Collections.enumeration(headers.keySet());
	}

}
