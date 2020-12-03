package com.hybris.caas.data.config;

import com.hybris.caas.data.persistence.converter.AesEncryptionConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AesEncryptionProperties.class)
@ConditionalOnProperty(name = { "caas.encryption.aes.key", "caas.encryption.aes.salt" })
public class AesEncryptionConfig
{
	@Bean
	public AesEncryptionConverter aesEncryptionConverter()
	{
		return new AesEncryptionConverter();
	}
}
