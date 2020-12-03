package com.hybris.caas.web.sort.util;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

import java.security.Principal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * An implementation of {@link NativeWebRequest} that simply delegates all behavior to the provided delegate.
 * <p>
 * The only difference is in how the {@link NativeWebRequest#getParameterValues(String)} works. Instead of retrieving
 * all of the parameters with the given parameter name, this implementation will retrieve a single parameter value
 * matching the parameter name. It will then split that single value into an array of strings using a comma as the
 * delimiter.
 * </p>
 */
public class CommaSeparatedParameterWebRequest implements NativeWebRequest
{
	private static final String COMMA = ",";
	private final NativeWebRequest delegate;

	public CommaSeparatedParameterWebRequest(final NativeWebRequest delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public Object getNativeRequest()
	{
		return this.delegate.getNativeRequest();
	}

	@Override
	public Object getNativeResponse()
	{
		return this.delegate.getNativeResponse();
	}

	@Override
	public <T> T getNativeRequest(final Class<T> requiredType)
	{
		return this.delegate.getNativeRequest(requiredType);
	}

	@Override
	public <T> T getNativeResponse(final Class<T> requiredType)
	{
		return this.delegate.getNativeResponse(requiredType);
	}

	@Override
	public String getHeader(final String headerName)
	{
		return this.delegate.getHeader(headerName);
	}

	@Override
	public String[] getHeaderValues(final String headerName)
	{
		return this.delegate.getHeaderValues(headerName);
	}

	@Override
	public Iterator<String> getHeaderNames()
	{
		return this.delegate.getHeaderNames();
	}

	@Override
	public String getParameter(final String paramName)
	{
		return this.delegate.getParameter(paramName);
	}

	@Override
	public String[] getParameterValues(final String paramName)
	{
		return Optional.ofNullable(this.delegate.getParameter(paramName))
				.map(param -> StringUtils.tokenizeToStringArray(param, COMMA, true, true))
				.orElse(null);
	}

	@Override
	public Iterator<String> getParameterNames()
	{
		return this.delegate.getParameterNames();
	}

	@Override
	public Map<String, String[]> getParameterMap()
	{
		return this.delegate.getParameterMap();
	}

	@Override
	public Locale getLocale()
	{
		return this.delegate.getLocale();
	}

	@Override
	public String getContextPath()
	{
		return this.delegate.getContextPath();
	}

	@Override
	public String getRemoteUser()
	{
		return this.delegate.getRemoteUser();
	}

	@Override
	public Principal getUserPrincipal()
	{
		return this.delegate.getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(final String role)
	{
		return this.delegate.isUserInRole(role);
	}

	@Override
	public boolean isSecure()
	{
		return this.delegate.isSecure();
	}

	@Override
	public boolean checkNotModified(final long lastModifiedTimestamp)
	{
		return this.delegate.checkNotModified(lastModifiedTimestamp);
	}

	@Override
	public boolean checkNotModified(final String etag)
	{
		return this.delegate.checkNotModified(etag);
	}

	@Override
	public boolean checkNotModified(final String etag, final long lastModifiedTimestamp)
	{
		return this.delegate.checkNotModified(etag, lastModifiedTimestamp);
	}

	@Override
	public String getDescription(final boolean includeClientInfo)
	{
		return this.delegate.getDescription(includeClientInfo);
	}

	@Override
	public Object getAttribute(final String name, final int scope)
	{
		return this.delegate.getAttribute(name, scope);
	}

	@Override
	public void setAttribute(final String name, final Object value, final int scope)
	{
		this.delegate.setAttribute(name, value, scope);
	}

	@Override
	public void removeAttribute(final String name, final int scope)
	{
		this.delegate.removeAttribute(name, scope);
	}

	@Override
	public String[] getAttributeNames(final int scope)
	{
		return this.delegate.getAttributeNames(scope);
	}

	@Override
	public void registerDestructionCallback(final String name, final Runnable callback, final int scope)
	{
		this.delegate.registerDestructionCallback(name, callback, scope);
	}

	@Override
	public Object resolveReference(final String key)
	{
		return this.delegate.resolveReference(key);
	}

	@Override
	public String getSessionId()
	{
		return this.delegate.getSessionId();
	}

	@Override
	public Object getSessionMutex()
	{
		return this.delegate.getSessionMutex();
	}
}
