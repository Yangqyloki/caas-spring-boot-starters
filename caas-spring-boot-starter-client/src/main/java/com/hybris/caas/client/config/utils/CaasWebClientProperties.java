package com.hybris.caas.client.config.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates configuration properties required for making REST calls to internal web services.
 */
@Component
@ConfigurationProperties(prefix = "caas.web.client")
@PropertySource("classpath:webclient.properties")
@Validated
public class CaasWebClientProperties
{
	@Valid
	private Properties properties = new Properties();
	@Valid
	private TokenCache tokenCache = new TokenCache();
	@Valid
	private UAA uaa = new UAA();

	public Properties getProperties()
	{
		return properties;
	}

	public void setProperties(final Properties properties)
	{
		this.properties = properties;
	}

	public TokenCache getTokenCache()
	{
		return tokenCache;
	}

	public void setTokenCache(final TokenCache tokenCache)
	{
		this.tokenCache = tokenCache;
	}

	public UAA getUaa()
	{
		return uaa;
	}

	public void setUaa(final UAA uaa)
	{
		this.uaa = uaa;
	}

	public static class Properties
	{
		/**
		 * The connection timeout in milliseconds.
		 */
		@Min(0)
		private int connectTimeoutMs;
		/**
		 * The read timeout in milliseconds.
		 */
		@Min(0)
		private int readTimeoutMs;

		@Valid
		private Retry retry = new Retry();

		@Valid
		private Obfuscate obfuscate = new Obfuscate();

		public int getConnectTimeoutMs()
		{
			return connectTimeoutMs;
		}

		public void setConnectTimeoutMs(final int connectTimeoutMs)
		{
			this.connectTimeoutMs = connectTimeoutMs;
		}

		public int getReadTimeoutMs()
		{
			return readTimeoutMs;
		}

		public void setReadTimeoutMs(final int readTimeoutMs)
		{
			this.readTimeoutMs = readTimeoutMs;
		}

		public Retry getRetry()
		{
			return retry;
		}

		public void setRetry(final Retry retry)
		{
			this.retry = retry;
		}

		public Obfuscate getObfuscate()
		{
			return obfuscate;
		}

		public void setObfuscate(final Obfuscate obfuscate)
		{
			this.obfuscate = obfuscate;
		}
	}

	public static class Retry
	{
		/**
		 * The number of times to tolerate an error.
		 */
		@Min(0)
		private int maxAttempts;

		public int getMaxAttempts()
		{
			return maxAttempts;
		}

		public void setMaxAttempts(final int maxAttempts)
		{
			this.maxAttempts = maxAttempts;
		}
	}

	public static class TokenCache
	{
		/**
		 * The max size of token cache.
		 */
		@Min(0)
		private int maxSize;

		/**
		 * The duration that an element is kept in the cache.
		 */
		@Min(0)
		private int expireAfterWrite;

		public int getMaxSize()
		{
			return maxSize;
		}

		public void setMaxSize(final int maxSize)
		{
			this.maxSize = maxSize;
		}

		public int getExpireAfterWrite()
		{
			return expireAfterWrite;
		}

		public void setExpireAfterWrite(final int expireAfterWrite)
		{
			this.expireAfterWrite = expireAfterWrite;
		}
	}

	public static class Obfuscate
	{
		/**
		 * The list of header that requires to be obfuscated.
		 */
		@Valid
		private List<String> headers = new ArrayList<>();

		public List<String> getHeaders()
		{
			return headers;
		}

		public void setHeaders(final List<String> headers)
		{
			this.headers = headers;
		}
	}

	/**
	 * User Authorization and Authentication.
	 * Uses a client credentials as grant type with a basic authentication method.
	 */
	public static class UAA
	{
		/**
		 * The client identifier
		 */
		@NotEmpty
		private String clientId;
		/**
		 * The client secret
		 */
		@NotEmpty
		private String clientSecret;

		/**
		 * The uri for the token endpoint template.
		 * The string {@code %s} will be replaced by the tenant identifier in the {@code tokenUriTemplate}.
		 */
		@NotEmpty
		private String tokenUriTemplate;

		public String getClientId()
		{
			return clientId;
		}

		public void setClientId(final String clientId)
		{
			this.clientId = clientId;
		}

		public String getClientSecret()
		{
			return clientSecret;
		}

		public void setClientSecret(final String clientSecret)
		{
			this.clientSecret = clientSecret;
		}

		public String getTokenUriTemplate()
		{
			return tokenUriTemplate;
		}

		public void setTokenUriTemplate(final String tokenUriTemplate)
		{
			this.tokenUriTemplate = tokenUriTemplate;
		}
	}
}
