package com.hybris.caas.web.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class AcceptHeaderFilterTest
{
	private MockHttpServletRequest request = new MockHttpServletRequest();
	private MockHttpServletResponse response = new MockHttpServletResponse();
	private MockFilterChain filterChain = new MockFilterChain();

	@Mock
	private ContentNegotiationManager contentNegotiationManager;
	@Captor
	private ArgumentCaptor<HttpServletRequest> argumentCaptor;

	private AcceptHeaderFilter filter;

	@Before
	public void setUp() throws HttpMediaTypeNotAcceptableException
	{
		filter = new AcceptHeaderFilter(contentNegotiationManager);

		when(contentNegotiationManager.resolveMediaTypes(any())).thenReturn(List.of(APPLICATION_XML));
	}

	@Test
	public void should_append_applicationJson_when_valid_one_already_present() throws ServletException, IOException
	{
		request.addHeader(ACCEPT, APPLICATION_XML_VALUE);
		filter.doFilterInternal(request, response, filterChain);

		final HeaderModifiableRequest result = (HeaderModifiableRequest) filterChain.getRequest();
		assertThat(result.getHeader(ACCEPT), equalTo(APPLICATION_XML_VALUE + ", " + APPLICATION_JSON_VALUE));
	}

	@Test
	public void should_set_applicationJson_when_invalid_one_present() throws ServletException, IOException
	{
		request.addHeader(ACCEPT, "invalid");
		when(contentNegotiationManager.resolveMediaTypes(any())).thenReturn(List.of());
		filter.doFilterInternal(request, response, filterChain);

		final HeaderModifiableRequest result = (HeaderModifiableRequest) filterChain.getRequest();
		assertThat(result.getHeader(ACCEPT), equalTo(APPLICATION_JSON_VALUE));
	}

	@Test
	public void should_not_append_applicationJson_when_already_present() throws ServletException, IOException
	{
		request.addHeader(ACCEPT, "application/xml, application/json");
		when(contentNegotiationManager.resolveMediaTypes(any())).thenReturn(List.of(APPLICATION_XML, APPLICATION_JSON));
		filter.doFilterInternal(request, response, filterChain);

		final MockHttpServletRequest result = (MockHttpServletRequest) filterChain.getRequest();
		assertThat(result.getHeader(ACCEPT), equalTo("application/xml, application/json"));
	}

}