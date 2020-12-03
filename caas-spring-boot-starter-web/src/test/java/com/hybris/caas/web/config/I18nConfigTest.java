package com.hybris.caas.web.config;

import com.hybris.caas.web.i18n.CaasLocaleResolver;
import com.hybris.caas.web.i18n.CaasResourceBundleMessageSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.LocaleResolver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class I18nConfigTest
{
	private I18nConfig i18nConfig = new I18nConfig();
	private I18nProperties i18nProperties = new I18nProperties();

	@Test
	public void should_create_custom_locale_resolver_bean()
	{
		final LocaleResolver localeResolver = i18nConfig.localeResolver(i18nProperties);
		assertThat(localeResolver, instanceOf(CaasLocaleResolver.class));
	}

	@Test
	public void should_create_locale_message_source_bean()
	{
		final I18nProperties i18nProperties = new I18nProperties();
		i18nProperties.setTranslationsPath("i18n/translations");
		i18nProperties.setPropertiesFileBaseName("messages");

		final CaasResourceBundleMessageSource messageSource = i18nConfig.caasMessageSource(i18nProperties);
		assertNotNull(messageSource);
	}
}
