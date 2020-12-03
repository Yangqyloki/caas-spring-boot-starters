package com.hybris.caas.test.config;

import com.hybris.caas.test.security.TokenFactory;
import com.hybris.caas.test.security.TokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:test.properties")
@EnableConfigurationProperties(TokenProperties.class)
public class TestConfig
{
	@Bean
	protected TokenFactory tokenFactory(final TokenProperties tokenProperties)
	{
		return new TokenFactory(tokenProperties);
	}

}
