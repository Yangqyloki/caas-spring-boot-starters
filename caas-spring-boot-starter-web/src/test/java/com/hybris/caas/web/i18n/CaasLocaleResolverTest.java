package com.hybris.caas.web.i18n;

import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;
import com.hybris.caas.web.Constants;
import com.hybris.caas.web.config.I18nProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

@ExtendWith(MockitoExtension.class)
public class CaasLocaleResolverTest
{
	@Mock
	private HttpServletRequest request;

	private CaasLocaleResolver localeResolver;
	private I18nProperties i18nProperties;

	@BeforeEach
	public void setUp()
	{
		i18nProperties = new I18nProperties();
		i18nProperties.getLocaleResolver().setAllowWildcard(true);
		i18nProperties.getLocaleResolver().setLenient(false);

		localeResolver = new CaasLocaleResolver(i18nProperties);
	}

	@Test
	public void should_not_support_setting_locale()
	{
		assertThrows(UnsupportedOperationException.class, () -> {
			localeResolver.setLocale(request, null, null);
		});
	}

	@Test
	public void should_return_en_US_locale_when_no_accept_language_header_is_provided()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("dummy");
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn(null);

		final Locale resolvedLocale = localeResolver.resolveLocale(request);
		assertThat(resolvedLocale, equalTo(Locale.US));
	}

	@Test
	public void should_return_all_languages_locale_constant_when_star_wild_card_is_provided()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn(" * ");

		final Locale resolvedLocale = localeResolver.resolveLocale(request);
		assertThat(resolvedLocale, equalTo(Constants.ALL_LANGUAGES_LOCALE));
	}

	@Test
	public void should_throw_exception_when_star_wild_card_is_provided_and_not_allow_wildcard_configured()
	{
		i18nProperties.getLocaleResolver().setAllowWildcard(false);
		localeResolver = new CaasLocaleResolver(i18nProperties);

		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn(" * ");

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_throw_exception_for_locale_with_invalid_language()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("und");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("und"));

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_return_invalid_locale_for_invalid_locale_and_not_lenient_configured()
	{
		i18nProperties.getLocaleResolver().setLenient(true);
		localeResolver = new CaasLocaleResolver(i18nProperties);

		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("und");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("und"));

		final Locale resolvedLocale = localeResolver.resolveLocale(request);
		assertThat(resolvedLocale, equalTo(Locale.forLanguageTag("und")));
	}

	@Test
	public void should_throw_exception_for_locale_without_country()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("en");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("en"));

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_throw_exception_for_locale_with_invalid_script_and_without_country()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("sr-Ltn");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("sr-Ltn"));

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_throw_exception_for_locale_with_script_and_without_country()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("sr-Latn");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("sr-Latn"));

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_throw_exception_for_invalid_locale()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("en-BT");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("en-BT"));

		InvalidHttpRequestHeaderException ex = assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
		assertTrue(ex.getMessage().contains("Accept-Language"));
	}

	@Test
	public void should_throw_exception_for_locale_that_does_not_require_script()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("en-Latn-US");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("en-Latn-US"));

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_throw_exception_for_locale_with_script_and_an_invalid_country()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("sr-Latn-RSA");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("sr-Latn-RSA"));

		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			localeResolver.resolveLocale(request);
		});
	}

	@Test
	public void should_resolve_locale()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("en-US");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("en-US"));

		final Locale resolvedLocale = localeResolver.resolveLocale(request);
		assertThat(resolvedLocale, equalTo(Locale.US));
	}

	@Test
	public void should_resolve_locale_with_script()
	{
		when(request.getHeader(ACCEPT_LANGUAGE)).thenReturn("sr-Latn-RS");
		when(request.getLocale()).thenReturn(Locale.forLanguageTag("sr-Latn-RS"));

		final Locale resolvedLocale = localeResolver.resolveLocale(request);

		assertEquals("sr", resolvedLocale.getLanguage());
		assertEquals("RS", resolvedLocale.getCountry());
		assertEquals("Latn", resolvedLocale.getScript());
	}

	@Test
	public void should_resolve_multi_locale_request()
	{
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.addHeader(ACCEPT_LANGUAGE, "da-DK,en-GB;q=0.8,fr-FR;q=0.7");

		final Locale resolveLocale = localeResolver.resolveLocale(mockRequest);

		assertEquals(Locale.forLanguageTag("da-DK"), resolveLocale);
	}
}
