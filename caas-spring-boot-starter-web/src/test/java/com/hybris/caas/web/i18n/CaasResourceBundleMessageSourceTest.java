package com.hybris.caas.web.i18n;

import com.hybris.caas.web.config.I18nProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;

public class CaasResourceBundleMessageSourceTest
{
	private static final Locale SERBIA_LOCALE = Locale.forLanguageTag("sr-Latn-RS");

	private CaasResourceBundleMessageSource messageSource;
	private I18nProperties i18nProperties;

	@Before
	public void setUp()
	{
		i18nProperties = new I18nProperties();
		i18nProperties.setPropertiesFileBaseName("messages");
		i18nProperties.setTranslationsPath("i18n/translations");
	}

	@Test(expected = IllegalStateException.class)
	public void should_throw_IllegalStateException_when_using_non_existing_path()
	{
		i18nProperties.setTranslationsPath("some/dummy/path");

		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();
	}

	@Test(expected = IllegalStateException.class)
	public void should_throw_IllegalStateException_when_loading_a_file_for_an_invalid_locale()
	{
		i18nProperties.setTranslationsPath("i18n/dummy");

		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();
	}

	@Test
	public void should_not_load_any_translations_when_using_invalid_file_base_name()
	{
		i18nProperties.setPropertiesFileBaseName("dummy");

		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();

		assertThat(messageSource.getAvailableLocales().size(), equalTo(0));
	}

	@Test
	public void should_load_translations()
	{
		final Locale pseudoTranslationsLocale = new Locale("dz", "BT");

		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();

		final Set<Locale> availableLocales = messageSource.getAvailableLocales();

		assertEquals(3, availableLocales.size());
		assertThat(availableLocales, hasItems(Locale.US, SERBIA_LOCALE, pseudoTranslationsLocale));

		final Map<String, String> usMessages = messageSource.getMessages(Locale.US);
		assertEquals(2, usMessages.size());
		assertThat(usMessages.keySet(), containsInAnyOrder("general.labels.order.subtotal", "general.labels.order.tax"));
		assertThat(usMessages.values(), containsInAnyOrder("Subtotal", "Taxes"));

		final Map<String, String> serbianMessages = messageSource.getMessages(SERBIA_LOCALE);
		assertEquals(3, serbianMessages.size());
		assertThat(serbianMessages.keySet(),
				containsInAnyOrder("general.labels.order.subtotal", "general.labels.order.tax", "general.labels.order.shippingFee"));
		assertThat(serbianMessages.values(), containsInAnyOrder("Subtotal-Latn", "Taxes-Latn", "Shipping Fee-Latn"));

		final Map<String, String> pseudoMessages = messageSource.getMessages(pseudoTranslationsLocale);
		assertEquals(6, pseudoMessages.size());
		assertThat(pseudoMessages.keySet(), containsInAnyOrder("general.labels.order.subtotal", "general.labels.order.shippingFee",
				"android.labels.product.collections.plusMore", "ios.labels.product.collections.plusMore",
				"pwa.labels.product.collections.plusMore", "general.buttons.product.collections.editItem"));
		assertThat(pseudoMessages.values(),
				containsInAnyOrder("[[[Ŝűƃţŏţąĺ∙∙∙∙∙∙]]]", "[[[Ŝĥįρρįŋğ∙∙∙∙∙∙]]]", "[[[+ %1$d ɱŏŗē]]]", "[[[+ %1$@ ɱŏŗē]]]",
						"[[[+ ${1} ɱŏŗē]]]", "[[[Ĕƌįţ įţēɱ∙∙∙∙∙]]]"));
	}

	@Test
	public void should_load_translations_for_keys()
	{
		final Locale pseudoTranslationsLocale = new Locale("dz", "BT");

		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();

		final Set<String> keys = Set.of("general.labels.order.subtotal", "general.labels.order.tax",
				"general.labels.order.shippingFee");
		final Map<Locale, Map<String, String>> localeMessagesMap = messageSource.getMessagesForAvailableLocales(keys);
		assertEquals(3, localeMessagesMap.size());
		assertThat(localeMessagesMap.keySet(), containsInAnyOrder(Locale.US, SERBIA_LOCALE, pseudoTranslationsLocale));

		final Map<String, String> usMessages = localeMessagesMap.get(Locale.US);
		assertEquals(2, usMessages.keySet().size());
		assertThat(usMessages.keySet(), containsInAnyOrder("general.labels.order.subtotal", "general.labels.order.tax"));
		assertThat(usMessages.values(), containsInAnyOrder("Subtotal", "Taxes"));

		final Map<String, String> serbianMessages = localeMessagesMap.get(SERBIA_LOCALE);
		assertEquals(3, serbianMessages.keySet().size());
		assertThat(serbianMessages.keySet(),
				containsInAnyOrder("general.labels.order.subtotal", "general.labels.order.tax", "general.labels.order.shippingFee"));
		assertThat(serbianMessages.values(), containsInAnyOrder("Subtotal-Latn", "Taxes-Latn", "Shipping Fee-Latn"));

		final Map<String, String> pseudoMessages = localeMessagesMap.get(pseudoTranslationsLocale);
		assertEquals(2, pseudoMessages.size());
		assertThat(pseudoMessages.keySet(), containsInAnyOrder("general.labels.order.subtotal", "general.labels.order.shippingFee"));
		assertThat(pseudoMessages.values(), containsInAnyOrder("[[[Ŝűƃţŏţąĺ∙∙∙∙∙∙]]]", "[[[Ŝĥįρρįŋğ∙∙∙∙∙∙]]]"));
	}

	@Test
	public void should_load_no_translations_for_non_existing_key()
	{
		final Locale pseudoTranslationsLocale = new Locale("dz", "BT");
		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();

		final Map<Locale, Map<String, String>> localeMessagesMap = messageSource.getMessagesForAvailableLocales(
				Collections.singleton("android.labels.product.collections.plusMore"));
		assertEquals(3, localeMessagesMap.size());

		final Map<String, String> usMessages = localeMessagesMap.get(Locale.US);
		assertEquals(0, usMessages.size());

		final Map<String, String> serbianMessages = localeMessagesMap.get(SERBIA_LOCALE);
		assertEquals(0, serbianMessages.size());

		final Map<String, String> pseudoMessages = localeMessagesMap.get(pseudoTranslationsLocale);
		assertEquals(1, pseudoMessages.size());
	}

	@Test
	public void should_load_no_translations_for_empty_keys()
	{
		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();

		final Map<Locale, Map<String, String>> localeMessagesMap = messageSource.getMessagesForAvailableLocales(
				Collections.emptySet());
		assertEquals(0, localeMessagesMap.size());
	}

	@Test
	public void should_load_no_translations_for_null_keys()
	{
		messageSource = new CaasResourceBundleMessageSource(i18nProperties);
		messageSource.loadAvailableTranslations();

		final Map<Locale, Map<String, String>> localeMessagesMap = messageSource.getMessagesForAvailableLocales(null);
		assertEquals(0, localeMessagesMap.size());
	}
}
