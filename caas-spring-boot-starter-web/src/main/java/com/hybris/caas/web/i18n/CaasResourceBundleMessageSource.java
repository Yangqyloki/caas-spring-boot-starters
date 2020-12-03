package com.hybris.caas.web.i18n;

import com.hybris.caas.web.config.I18nProperties;
import com.hybris.caas.web.validator.utils.LocaleUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CaasResourceBundleMessageSource
{
	private static final Logger LOG = LoggerFactory.getLogger(CaasResourceBundleMessageSource.class);

	private static final String FILE_NAME_LOCALE_SEPARATOR = "_";
	private static final String BASE_NAME_PATH_SEPARATOR = "/";
	private static final String PROPERTIES_EXTENSION = ".properties";

	private I18nProperties i18nProperties;
	private CaasReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource;
	private Map<Locale, Map<String, String>> localeMessageSource;

	public CaasResourceBundleMessageSource(final I18nProperties i18nProperties)
	{
		this.i18nProperties = i18nProperties;
		this.reloadableResourceBundleMessageSource = new CaasReloadableResourceBundleMessageSource();

		this.reloadableResourceBundleMessageSource.setBasename(
				i18nProperties.getTranslationsPath() + BASE_NAME_PATH_SEPARATOR + i18nProperties.getPropertiesFileBaseName());
		this.reloadableResourceBundleMessageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		this.reloadableResourceBundleMessageSource.setFallbackToSystemLocale(false);
	}

	@PostConstruct
	void loadAvailableTranslations()
	{
		LOG.info("Started to load i18n translations...");

		final Map<Locale, Map<String, String>> localeTranslations = new HashMap<>();
		final String translationFilePrefix = i18nProperties.getPropertiesFileBaseName() + FILE_NAME_LOCALE_SEPARATOR;
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		try
		{
			final Resource[] resources = resolver.getResources("classpath:" + i18nProperties.getTranslationsPath()
					+ BASE_NAME_PATH_SEPARATOR + translationFilePrefix + "*" + PROPERTIES_EXTENSION);

			final List<String> translationFilesNames = new ArrayList<>();
			Arrays.stream(resources).forEach(resource -> translationFilesNames.add(resource.getFilename()));

			if (!translationFilesNames.isEmpty())
			{
				translationFilesNames.stream().forEach(file -> {
					final Locale locale = getLocaleFromFilename(file);

					final Properties properties = reloadableResourceBundleMessageSource.getProperties(locale);
					final Map<String, String> translatedProperties = new HashMap<>();
					properties.stringPropertyNames()
							.forEach(property -> translatedProperties.put(property, properties.getProperty(property)));

					localeTranslations.put(locale, Collections.unmodifiableMap(translatedProperties));
				});
			}
		}
		catch (final IOException e)
		{
			throw new IllegalStateException(String.format("Unable to load translation files from folder %s with prefix %s.",
					i18nProperties.getTranslationsPath(), i18nProperties.getPropertiesFileBaseName()), e);
		}

		// No reason to keep the cached resources
		this.reloadableResourceBundleMessageSource.clearCacheIncludingAncestors();

		LOG.info("I18n translations loaded for the following locales: {}", localeTranslations.keySet());

		this.localeMessageSource = Collections.unmodifiableMap(localeTranslations);
	}

	/**
	 * Returns the translated labels for a particular locale.
	 *
	 * @param locale the locale for which to get the translated labels
	 * @return the translated labels for a particular locale
	 */
	public Map<String, String> getMessages(final Locale locale)
	{
		return this.localeMessageSource.get(locale);
	}

	/**
	 * Returns the map of available locales and the associated translated labels for given keys
	 *
	 * @param keys the keys to translate
	 * @return map of available locales and the associated translated labels for given keys
	 */
	public Map<Locale, Map<String, String>> getMessagesForAvailableLocales(final Set<String> keys)
	{
		return CollectionUtils.isEmpty(keys) ?
				new HashMap<>() :
				localeMessageSource.entrySet()
						.stream()
						.collect(Collectors.toMap(Map.Entry::getKey, localeMapEntry -> localeMapEntry.getValue()
								.entrySet()
								.stream()
								.filter(propertyEntry -> keys.contains(propertyEntry.getKey()))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
	}

	/**
	 * Returns the set of locales for which label translations are available.
	 *
	 * @return the set of locales for which label translations are available
	 */
	public Set<Locale> getAvailableLocales()
	{
		return this.localeMessageSource.keySet();
	}

	private Locale getLocaleFromFilename(final String fileName)
	{
		final Locale locale = Locale.forLanguageTag(
				StringUtils.substringAfter(FilenameUtils.removeExtension(fileName), FILE_NAME_LOCALE_SEPARATOR).replace("_", "-"));
		if (LocaleUtils.isLocaleValid(locale))
		{
			return locale;
		}
		else
		{
			throw new IllegalStateException(String.format("Invalid translation file. \"%s\" specifies an invalid locale.", fileName));
		}
	}

	private static class CaasReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource
	{
		public Properties getProperties(final Locale locale)
		{
			final PropertiesHolder propertiesHolder = super.getMergedProperties(locale);
			return propertiesHolder.getProperties();
		}

		@Override
		protected List<String> calculateFilenamesForLocale(final String basename, final Locale locale)
		{
			// the method from the parent class does not support script component of the locale; just variants.
			final List<String> calculatedFilenames = super.calculateFilenamesForLocale(basename, locale);

			if (StringUtils.isNotEmpty(locale.getScript()))
			{
				final String scriptBasedFilename = String.format("%s_%s_%s_%s", basename, locale.getLanguage(), locale.getScript(),
						locale.getCountry());
				final List<String> filenames = new ArrayList<>(calculatedFilenames.size() + 1);
				filenames.add(scriptBasedFilename);
				filenames.addAll(calculatedFilenames);

				return filenames;
			}

			return calculatedFilenames;
		}
	}
}
