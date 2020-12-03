package com.hybris.caas.web.config;

import com.hybris.caas.web.i18n.CaasLocaleResolver;
import com.hybris.caas.web.i18n.CaasResourceBundleMessageSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfig
{
	@Bean
	@ConditionalOnProperty(name = "caas.i18n.locale-resolver.enabled", havingValue = "true")
	@ConditionalOnMissingBean(name = "localeResolver")
	public LocaleResolver localeResolver(final I18nProperties i18nProperties)
	{
		return new CaasLocaleResolver(i18nProperties);
	}

	@Bean
	@ConditionalOnProperty(name = { "caas.i18n.translations-path", "caas.i18n.properties-file-base-name" })
	public CaasResourceBundleMessageSource caasMessageSource(final I18nProperties i18nProperties)
	{
		return new CaasResourceBundleMessageSource(i18nProperties);
	}
}
