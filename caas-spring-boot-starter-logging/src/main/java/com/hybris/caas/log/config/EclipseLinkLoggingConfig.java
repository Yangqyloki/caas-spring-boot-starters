package com.hybris.caas.log.config;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConditionalOnClass(AbstractSessionLog.class)
@PropertySource("classpath:eclipselink.properties")
public class EclipseLinkLoggingConfig
{
	// Nothing to do, only to import properties in low level way.
}
