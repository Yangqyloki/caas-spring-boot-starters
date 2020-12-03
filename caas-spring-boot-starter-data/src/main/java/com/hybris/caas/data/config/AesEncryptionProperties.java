package com.hybris.caas.data.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "caas.encryption.aes")
public class AesEncryptionProperties
{
	/**
	 * The Aes key used to generate the encryptor's secret key; should not be shared.
	 */
	private String key;

	/**
	 * The Aes salt used a hex-encoded, random, site-global salt value to use to generate the secret key.
	 */
	private String salt;

	public String getKey()
	{
		return key;
	}

	public void setKey(final String key)
	{
		this.key = key;
	}

	public String getSalt()
	{
		return salt;
	}

	public void setSalt(final String salt)
	{
		this.salt = salt;
	}
}
