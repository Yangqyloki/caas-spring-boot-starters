package com.hybris.caas.log.config;

import brave.propagation.CurrentTraceContext;
import com.hybris.caas.log.tracing.CaasSlf4jCurrentTraceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConditionalOnWebApplication
@PropertySource("classpath:sleuth.properties")
@PropertySource("classpath:logging.properties")
public class LoggingConfig
{
	@Bean
	public CurrentTraceContext.ScopeDecorator slf4jSpanLoggerScopeDecorator()
	{
		return new CaasSlf4jCurrentTraceContext();
	}
}
