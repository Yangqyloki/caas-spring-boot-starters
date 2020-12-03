package com.hybris.caas.log.config;

import com.hybris.caas.log.audit.service.DataChangeObjectProcessor;
import com.hybris.caas.log.audit.service.FullDataChangeObjectProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import com.hybris.caas.log.audit.aspect.ConfigurationChangeAspect;
import com.hybris.caas.log.audit.aspect.SecurityEventAspect;
import com.hybris.caas.log.audit.service.AsyncAuditLogger;
import com.hybris.caas.log.audit.service.AuditLogger;
import com.hybris.caas.log.audit.service.AuditLoggerTransactionalDecorator;
import com.hybris.caas.log.context.TracingUserProvider;
import com.hybris.caas.log.context.UserProvider;
import com.sap.xs.audit.api.exception.AuditLogException;
import com.sap.xs.audit.api.v2.AuditLogMessageFactory;
import com.sap.xs.audit.client.impl.v2.AuditLogMessageFactoryImpl;
import com.sap.xs.env.VcapServices;

import brave.Tracing;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuditLogConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(AuditLogConfig.class);
	static final String AUDIT_LOG_SERVICE_NAME = "auditlog";

	@Bean
	public AuditLogProperties auditLogProperties()
	{
		return new AuditLogProperties();
	}

	@Bean
	@ConditionalOnMissingBean(UserProvider.class)
	public UserProvider tracingUserProvider(final Tracing tracing)
	{
		return new TracingUserProvider(tracing);
	}

	@Bean
	public DataChangeObjectProcessor dataChangeObjectProcessor()
	{
		return new FullDataChangeObjectProcessor();
	}
	
	@Bean
	public AuditLogger auditLogger(final AuditLogProperties auditLogProperties, final UserProvider userProvider, 
			final DataChangeObjectProcessor dataChangeObjectProcessor) throws AuditLogException
	{
		final AuditLogMessageFactory auditLogMessageFactory;
		if (auditLogProperties.isEnabled())
		{
			final VcapServices vcapServices = getVcapServicesFromEnvironment();
			if (!vcapServices.isServiceBound(AUDIT_LOG_SERVICE_NAME, null, null))
			{
				throw new AuditLogException("Cloud Foundry audit log service is enabled, but not bound to this application. "
						+ "If you wish to disable this service and log to the console instead, then please set 'sap.audit.service.enabled=false'.");
			}
			auditLogMessageFactory = new AuditLogMessageFactoryImpl(vcapServices);
		}
		else
		{
			auditLogMessageFactory = new AuditLogMessageFactoryImpl(new VcapServices());
			LOG.warn("Property 'sap.audit.service.enabled' is set to false. Please ensure this is always set to 'true' in production environments.");
		}
		return new AsyncAuditLogger(auditLogProperties, auditLogMessageFactory, dataChangeObjectProcessor, userProvider);
	}

	@Bean
	@Primary
	public AuditLogger auditLoggerTransactionalDecorator(@Qualifier("auditLogger") final AuditLogger auditLoggerToDecorate)
	{
		return new AuditLoggerTransactionalDecorator(auditLoggerToDecorate);
	}

	@Bean
	public ConfigurationChangeAspect configurationChangeAspect(final AuditLogger auditLogger)
	{
		return new ConfigurationChangeAspect(auditLogger);
	}

	@Bean
	public SecurityEventAspect securityEventAspect(final AuditLogger auditLogger)
	{
		return new SecurityEventAspect(auditLogger);
	}

	VcapServices getVcapServicesFromEnvironment()
	{
		return VcapServices.fromEnvironment();
	}
}
