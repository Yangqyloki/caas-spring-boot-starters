package com.hybris.caas.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "caas.i18n")
@PropertySource("classpath:i18n.properties")
public class I18nProperties
{
	private String translationsPath;
	private String propertiesFileBaseName;
	private LocaleResolverConfig localeResolver = new LocaleResolverConfig();

	public String getPropertiesFileBaseName()
	{
		return propertiesFileBaseName;
	}

	public void setPropertiesFileBaseName(final String propertiesFileBaseName)
	{
		this.propertiesFileBaseName = propertiesFileBaseName;
	}

	public String getTranslationsPath()
	{
		return translationsPath;
	}

	public void setTranslationsPath(final String translationsPath)
	{
		this.translationsPath = translationsPath;
	}

	public LocaleResolverConfig getLocaleResolver()
	{
		return localeResolver;
	}

	public void setLocaleResolver(final LocaleResolverConfig localeResolver)
	{
		this.localeResolver = localeResolver;
	}

	public static class LocaleResolverConfig
	{
		private boolean enabled;
		private boolean allowWildcard;
		private boolean lenient;

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(final boolean enabled)
		{
			this.enabled = enabled;
		}

		public boolean isAllowWildcard()
		{
			return allowWildcard;
		}

		public void setAllowWildcard(final boolean allowWildcard)
		{
			this.allowWildcard = allowWildcard;
		}

		public boolean isLenient()
		{
			return lenient;
		}

		public void setLenient(final boolean lenient)
		{
			this.lenient = lenient;
		}
	}
}
