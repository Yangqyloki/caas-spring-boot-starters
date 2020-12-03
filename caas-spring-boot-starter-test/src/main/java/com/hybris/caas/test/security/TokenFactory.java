package com.hybris.caas.test.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokenFactory
{
	private final TokenProperties tokenProperties;

	public TokenFactory(final TokenProperties tokenProperties)
	{
		this.tokenProperties = tokenProperties;
	}

	Map<String, String> getAllTokens()
	{
		final Map<String, String> tokens = new HashMap<>();
		Optional.ofNullable(tokenProperties.getTokens())
				.ifPresent(tokensMap -> tokensMap.keySet().forEach(key -> tokens.put(key, getToken(key))));
		return tokens;
	}

	public CaasJwtToken getTokenObject(final String tokenName)
	{
		return Optional.ofNullable(tokenProperties.getTokens())
				.map(tokens -> tokens.get(tokenName))
				.map(CaasJwtToken::of)
				.orElse(null);
	}

	public String getToken(final String tokenName)
	{
		return Optional.ofNullable(getTokenObject(tokenName)).map(CaasJwtToken::encode).orElse(null);
	}

}
