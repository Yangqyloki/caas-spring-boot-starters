package com.hybris.caas.multitenant.service;

import com.sap.cloud.security.xsuaa.token.SpringSecurityContext;
import com.sap.cloud.security.xsuaa.token.Token;
import org.springframework.stereotype.Component;

/**
 * Provides access to common user related attributes from the SAP JWT token.
 */
@Component
public class SapJwtTokenProvider implements TokenProvider
{
	@Override
	public Token getToken()
	{
		return SpringSecurityContext.getToken();
	}
}
