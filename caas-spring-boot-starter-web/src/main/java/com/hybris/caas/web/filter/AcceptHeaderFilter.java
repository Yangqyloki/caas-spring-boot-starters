package com.hybris.caas.web.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT;

/**
 * Inspects the {@code Accept} header and appends the {@code application/json} media type if it is not already present.
 */
@Component
public class AcceptHeaderFilter extends OncePerRequestFilter
{
	private final ContentNegotiationManager contentNegotiationManager;

	@Autowired
	public AcceptHeaderFilter(final ContentNegotiationManager contentNegotiationManager)
	{
		this.contentNegotiationManager = contentNegotiationManager;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException
	{
		final List<MediaType> mediaTypes = contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
		if (!mediaTypes.contains(MediaType.APPLICATION_JSON))
		{
			// Create new list in case the original one non-modifiable.
			final List<MediaType> newMediaTypes = new ArrayList<>(mediaTypes);
			newMediaTypes.add(MediaType.APPLICATION_JSON);

			final HeaderModifiableRequest headerModifiableRequest = new HeaderModifiableRequest(request);
			headerModifiableRequest.setHeader(ACCEPT, MediaType.toString(newMediaTypes));

			filterChain.doFilter(headerModifiableRequest, response);
		}
		else
		{
			filterChain.doFilter(request, response);
		}
	}

}

