package com.hybris.caas.multitenant.service;

import com.sap.cloud.security.xsuaa.token.Token;
import org.springframework.security.access.AccessDeniedException;

/**
 * Provides access to common user related attributes extracted from the JWT token.
 */
public interface TokenProvider
{
	/**
	 * Get the token
	 *
	 * @return the token object
	 * @throws AccessDeniedException when there is no token
	 */
	Token getToken();
}
