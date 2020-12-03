package com.hybris.caas.web.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

@Configuration
public class JacksonModuleConfig
{
	@Bean
	public Module offsetDateTimeModule()
	{
		final SimpleModule module = new SimpleModule("OffsetDateTimeModule");
		module.addSerializer(OffsetDateTime.class, new CustomOffsetDateTimeSerializer());
		return module;
	}
}
