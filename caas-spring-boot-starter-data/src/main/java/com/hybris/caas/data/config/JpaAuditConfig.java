package com.hybris.caas.data.config;

import com.hybris.caas.data.audit.CustomDateTimeProvider;
import com.hybris.caas.data.jpa.audit.AuditorAwareImpl;
import com.hybris.caas.log.context.UserProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "customDateTimeProvider")
public class JpaAuditConfig
{
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public AuditorAware<String> auditorAware(final UserProvider userProvider)
	{
		return new AuditorAwareImpl(userProvider);
	}

	@Bean
	public DateTimeProvider customDateTimeProvider()
	{
		return new CustomDateTimeProvider();
	}
}
