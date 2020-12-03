package com.hybris.caas.web;

import java.util.Locale;

/**
 * Defines constants used by caas-spring-boot-starter-web.
 */
public class Constants
{
	public static final String STAR_WILDCARD = "*";
	public static final String IDENTIFIER_REG_EXP = "^[0-9A-Fa-f]{8}-([0-9A-Fa-f]{4}-){3}[0-9A-Fa-f]{12}$";
	public static final String LOCALE_REG_EXP = "^[a-z]{2}-[A-Z]{2}$";
	public static final String LOCALE_WITH_SCRIPT_REG_EXP = "^[a-z]{2}-[A-Z]{1}[a-z]{3}-[A-Z]{2}$";

	/**
	 * String representation of the US locale.
	 */
	public static final String US_LOCALE_TAG = "en-US";

	/**
	 * Locale to match all languages.
	 */
	public static final Locale ALL_LANGUAGES_LOCALE = new Locale(STAR_WILDCARD);

	private Constants()
	{
		// private constructor
	}
}
