package com.hybris.caas.kafka.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RandomUuidGeneratorPostProcessor implements EnvironmentPostProcessor
{
	@Override
	public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application)
	{
		final MutablePropertySources propertySources = environment.getPropertySources();
		final Map<String, Object> properties = new HashMap<>();
		properties.put("caas.random.uuid", UUID.randomUUID().toString());
		propertySources.addFirst(new MapPropertySource(RandomUuidGeneratorPostProcessor.class.getSimpleName(), properties));
	}
}
