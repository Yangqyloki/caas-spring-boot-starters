package com.hybris.caas.multitenant.filter;

import com.hybris.caas.multitenant.Constants;
import com.hybris.caas.multitenant.service.TokenProvider;
import com.hybris.caas.multitenant.service.config.TenantProperties;
import com.hybris.caas.multitenant.service.exception.InvalidTenantFormatException;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import com.sap.cloud.security.xsuaa.token.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import static com.hybris.caas.multitenant.filter.TenantHeaderFilter.X_FORWARDED_HOST;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TenantHeaderFilterTest
{
	private static final String CAAS_INTEGRATION_TEST = "caas-integration-test";
	private static final String DUMMY_HOST = ".dummy.host";
	private static final String FORWARDED_HOST_REGEX = "^([a-zA-Z0-9-]+)" + DUMMY_HOST + "$";
	private static final String FORWARDED_HOST_REGEX_2 = "^([a-zA-Z0-9-]+)-([a-zA-Z0-9-]+)" + DUMMY_HOST + "$";
	private static final String FORWARDED_HOST_REGEX_3 = "^([%]+)" + DUMMY_HOST + "$";
	private static final String DUMMY_TENANT = "dummy-tenant";
	private static final String DUMMY_TENANT_FOR_LOCALE_TESTING = "caas-tenant-with-TURKISH-locale";
	private static final String DUMMY_TENANT_FOR_TURKISH_LOCALE_LOWER_CASE = "caas-tenant-with-turkısh-locale";
	private static final String DUMMY_TENANT_FOR_ENGLISH_LOCALE_LOWER_CASE = "caas-tenant-with-turkish-locale";
	private static final String PAAS_TENANT = "paas-tenant";
	private static final String DUMMY_INVALID_CHAR_TENANT = "%";

	@Mock
	private TokenProvider tokenProvider;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;
	@Mock
	private Token token;

	private MockHttpServletRequest request = new MockHttpServletRequest();
	private TenantProperties tenantProperties;
	private TenantProperties tenantPropertiesWithPathProperties;
	private TenantHeaderFilter tenantHeaderFilter;


	private TenantProperties failureTenantPropertiesWithPathProperties;


	@Before
	public void setUp()
	{
		buildTenantProperties();

		when(token.getSubdomain()).thenReturn(CAAS_INTEGRATION_TEST);
		when(tokenProvider.getToken()).thenReturn(token);
		tenantHeaderFilter = new TenantHeaderFilter(tenantProperties, tokenProvider);
	}

	private void buildTenantProperties()
	{
		failureTenantPropertiesWithPathProperties = new TenantProperties();
		failureTenantPropertiesWithPathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX_3);
		failureTenantPropertiesWithPathProperties.setPaasName(PAAS_TENANT);

		tenantProperties = new TenantProperties();
		tenantProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		tenantProperties.setPaasName(PAAS_TENANT);

		failureTenantPropertiesWithPathProperties.setPaasName(PAAS_TENANT);

		tenantPropertiesWithPathProperties = new TenantProperties();
		tenantPropertiesWithPathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		tenantPropertiesWithPathProperties.setPaasName(PAAS_TENANT);
		TenantProperties.TenantPathProperties first = new TenantProperties.TenantPathProperties();
		first.setPathPattern("/foo/**/*");
		first.setForwardedHostRegex(FORWARDED_HOST_REGEX_2);
		first.setCapturingGroup(1);
		TenantProperties.TenantPathProperties second = new TenantProperties.TenantPathProperties();
		second.setPathPattern("/.well-known/**/*");
		second.setForwardedHostRegex(FORWARDED_HOST_REGEX_2);
		second.setCapturingGroup(2);
		tenantPropertiesWithPathProperties.setPaths(Arrays.asList(first, second));
	}

	@Test
	public void should_return_tenant_from_JWT() throws ServletException, IOException
	{
		request.addHeader(HttpHeaders.AUTHORIZATION, "BeArEr");

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		final String tenant = (String) request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME);
		assertThat(tenant, is(CAAS_INTEGRATION_TEST));
	}

	@Test
	public void should_throw_MissingTenantException_for_bad_JWT() throws ServletException, IOException
	{
		when(tokenProvider.getToken()).thenThrow(new AccessDeniedException("Access denied"));

		request.addHeader(HttpHeaders.AUTHORIZATION, "BEARER");

		try
		{
			tenantHeaderFilter.doFilterInternal(request, response, filterChain);
			fail();
		}
		catch (final MissingTenantException e)
		{
			assertTrue(MissingTenantException.AccessType.PROTECTED.equals(e.getAccessType()));
		}
	}

	@Test
	public void should_throw_invalid_tenant_format_exception() throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, DUMMY_INVALID_CHAR_TENANT + DUMMY_HOST);
		tenantHeaderFilter = new TenantHeaderFilter(failureTenantPropertiesWithPathProperties, tokenProvider);

		try
		{
			tenantHeaderFilter.doFilterInternal(request, response, filterChain);
			fail();
		}
		catch (final InvalidTenantFormatException e)
		{
			assert(true);
		}
	}


	@Test
	public void should_return_tenant_from_forwarded_host_header_without_path_properties() throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, DUMMY_TENANT + DUMMY_HOST);

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		final String tenant = (String) request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME);
		assertThat(tenant, is(DUMMY_TENANT));
	}

	@Test
	public void should_continue_chain_filter_when_forwarded_host_header_is_not_available() throws ServletException, IOException
	{
		assertMissingTenant();
	}

	@Test
	public void should_continue_chain_filter_when_forwarded_host_header_does_not_match_regex() throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, "invalid.value");

		assertMissingTenant();
	}

	@Test
	public void should_continue_filter_chain_if_authentication_is_not_oAuth2() throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, DUMMY_TENANT + DUMMY_HOST);
		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic blah blah blah");

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_get_tenant_lowercase_from_JWT_matches_FORWARDED_different_casing() throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, "caas-integration-TEST" + DUMMY_HOST);
		request.addHeader(HttpHeaders.AUTHORIZATION, DUMMY_TENANT);

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		final String tenant = (String) request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME);
		assertThat(tenant, is(CAAS_INTEGRATION_TEST));
	}

	@Test
	public void should_return_tenant_from_forwarded_host_header_with_path_properties_no_match() throws ServletException, IOException
	{
		tenantHeaderFilter = new TenantHeaderFilter(tenantPropertiesWithPathProperties, tokenProvider);

		request.addHeader(X_FORWARDED_HOST, DUMMY_TENANT + DUMMY_HOST);

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		final String tenant = (String) request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME);
		assertThat(tenant, is(DUMMY_TENANT));
	}

	@Test
	public void should_return_tenant_from_forwarded_host_header_with_path_properties_with_match() throws ServletException, IOException
	{
		tenantHeaderFilter = new TenantHeaderFilter(tenantPropertiesWithPathProperties, tokenProvider);

		request.addHeader(X_FORWARDED_HOST, DUMMY_TENANT + DUMMY_HOST);
		request.setServletPath("/.well-known/some-apple-pay-thing.txt");

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		final String tenant = (String) request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME);
		assertThat(tenant, is("tenant"));
	}

	@Test
	public void assert_lower_case_when_getting_tenant_from_header_is_correctly_converting_to_lower_case_using_english_locale_by_asserting_different_when_using_turkish_locale()
			throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, DUMMY_TENANT_FOR_LOCALE_TESTING + DUMMY_HOST);

		assertLowerCaseConversionIsDifferentWhenUsingTurkishLocale();
	}

	@Test
	public void assert_lower_case_when_getting_tenant_from_token_is_correctly_converting_to_lower_case_using_english_locale_by_asserting_different_when_using_turkish_locale()
			throws ServletException, IOException
	{
		request.addHeader(X_FORWARDED_HOST, DUMMY_TENANT_FOR_LOCALE_TESTING);
		request.addHeader(HttpHeaders.AUTHORIZATION, "bearer " + DUMMY_TENANT_FOR_LOCALE_TESTING);
		when(token.getSubdomain()).thenReturn(DUMMY_TENANT_FOR_LOCALE_TESTING);

		assertLowerCaseConversionIsDifferentWhenUsingTurkishLocale();
	}

	private void assertMissingTenant() throws ServletException, IOException
	{
		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		assertThat(request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME), is(nullValue()));
	}

	private void assertLowerCaseConversionIsDifferentWhenUsingTurkishLocale() throws ServletException, IOException
	{
		final Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.forLanguageTag("TR"));

		final String tenantWithTurkishLocale = DUMMY_TENANT_FOR_LOCALE_TESTING.toLowerCase();
		assertEquals(DUMMY_TENANT_FOR_TURKISH_LOCALE_LOWER_CASE, tenantWithTurkishLocale);

		tenantHeaderFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		final String tenantWithEnglishLocale = (String) request.getAttribute(Constants.TENANT_ATTRIBUTE_NAME);

		assertEquals(DUMMY_TENANT_FOR_ENGLISH_LOCALE_LOWER_CASE, tenantWithEnglishLocale);
		assertNotEquals(tenantWithEnglishLocale, tenantWithTurkishLocale);

		Locale.setDefault(defaultLocale);
	}
}
