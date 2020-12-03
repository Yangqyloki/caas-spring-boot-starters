package com.hybris.caas.test.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ConfigurationProperties(prefix = "sap.security")
public class TokenProperties
{
	private static final String PLACEHOLDER_NOW = "${now}";
	private static final String PLACEHOLDER_UUID = "${uuid}";

	private Map<String, SingleTokenProperties> tokens;

	public Map<String, SingleTokenProperties> getTokens()
	{
		return tokens;
	}

	public void setTokens(final Map<String, SingleTokenProperties> tokens)
	{
		this.tokens = tokens;
	}

	public static class SingleTokenProperties
	{
		private String tenant;
		private String identityZone;
		private String privateKey;
		private String accountId;
		private String email;
		private List<String> scopes;

		public String getTenant()
		{
			return Optional.ofNullable(tenant)
					.map(value -> value.replace(PLACEHOLDER_NOW, Long.toString(System.nanoTime())).replace(PLACEHOLDER_UUID, UUID.randomUUID().toString()))
					.orElse(null);
		}

		public void setTenant(final String tenant)
		{
			this.tenant = tenant;
		}

		public String getPrivateKey()
		{
			return privateKey;
		}

		public void setPrivateKey(final String privateKey)
		{
			this.privateKey = privateKey;
		}

		public String getAccountId()
		{
			return Optional.ofNullable(accountId)
					.map(value -> value.replace(PLACEHOLDER_NOW, Long.toString(System.nanoTime())).replace(PLACEHOLDER_UUID, UUID.randomUUID().toString()))
					.orElse(null);
		}

		public void setAccountId(final String accountId)
		{
			this.accountId = accountId;
		}

		public String getEmail()
		{
			return email;
		}

		public void setEmail(final String email)
		{
			this.email = email;
		}

		public List<String> getScopes()
		{
			return scopes;
		}

		public void setScopes(final List<String> scopes)
		{
			this.scopes = scopes;
		}

		public String getIdentityZone()
		{
			return identityZone;
		}

		public void setIdentityZone(final String identityZone)
		{
			this.identityZone = identityZone;
		}
	}
}
