package com.hybris.caas.web.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;

import static com.hybris.caas.web.filter.ForwardedPathFilter.X_FORWARDED_PATH;
import static com.hybris.caas.web.filter.ForwardedPathFilter.X_FORWARDED_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

@RunWith(MockitoJUnitRunner.class)
public class ForwardedPathFilterTest
{
	private MockHttpServletRequest request = new MockHttpServletRequest();
	private MockHttpServletResponse response = new MockHttpServletResponse();
	private MockFilterChain filterChain = new MockFilterChain();

	private ForwardedPathFilter filter = new ForwardedPathFilter();

	@Test
	public void shouldAddForwardedPrefixHeader() throws Exception
	{
		request.addHeader(X_FORWARDED_PATH, "/public/product-content/products");
		request.setServletPath("/products");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(filterChain.getRequest(), is(instanceOf(HttpServletRequest.class)));
		final HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();

		assertThat(result.getHeader(X_FORWARDED_PREFIX), equalTo("/public/product-content"));
	}

	@Test
	public void shouldAddForwardedPrefixHeaderEvenWithRequestUriEndingWithSlash() throws Exception
	{
		request.addHeader(X_FORWARDED_PATH, "/public/product-content/products");
		request.setServletPath("/products/");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(filterChain.getRequest(), is(instanceOf(HttpServletRequest.class)));
		final HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();

		assertThat(result.getHeader(X_FORWARDED_PREFIX), equalTo("/public/product-content"));
	}

	@Test
	public void shouldNotAddForwardedPrefixIfForwardedPathNotPresent() throws Exception
	{
		request.setServletPath("/products/");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(filterChain.getRequest(), is(instanceOf(HttpServletRequest.class)));
		final HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();

		assertThat(result.getHeader(X_FORWARDED_PREFIX), is(nullValue()));
	}

	@Test
	public void shouldContinueIfForwardedPrefixPresent() throws Exception
	{
		request.addHeader(X_FORWARDED_PREFIX, "/public/product-content");
		request.addHeader(X_FORWARDED_PATH, "/public/product-content/products");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(filterChain.getRequest(), is(instanceOf(HttpServletRequest.class)));
		final HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();

		assertThat("The original request was NOT sent to the given filterChain", result, is(sameInstance(request)));
	}

	@Test
	public void shouldAddForwardedPrefixHeaderHandlingMultipleDashesTogether() throws Exception
	{
		request.addHeader(X_FORWARDED_PATH, "/public/content-repository/applications/guid/////download");
		request.setServletPath("/applications/guid/download/"); // won't have more than one slash together, spring cleans it up

		filter.doFilterInternal(request, response, filterChain);

		assertThat(filterChain.getRequest(), is(instanceOf(HttpServletRequest.class)));
		final HttpServletRequest result = (HttpServletRequest) filterChain.getRequest();

		assertThat(result.getHeader(X_FORWARDED_PREFIX), equalTo("/public/content-repository"));
	}
}

