package com.hybris.caas.data.config;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring configurations for JPA/EclipseLink.
 */
@Configuration
@ConditionalOnClass(org.eclipse.persistence.Version.class)
public class EclipseLinkJpaConfig extends CaaSJpaBaseConfiguration
{
	protected EclipseLinkJpaConfig(DataSource dataSource, JpaProperties properties,
			ObjectProvider<JtaTransactionManager> jtaTransactionManagerProvider)
	{
		super(dataSource, properties, jtaTransactionManagerProvider);
	}

	@Override
	protected AbstractJpaVendorAdapter createJpaVendorAdapter()
	{
		return new EclipseLinkJpaVendorAdapter();
	}

	@Override
	protected Map<String, Object> getVendorProperties()
	{
		final HashMap<String, Object> props = new HashMap<>();
		props.put(PersistenceUnitProperties.WEAVING, detectWeavingMode() ? "true" : "static");
		return props;
	}

	protected boolean detectWeavingMode()
	{
		return InstrumentationLoadTimeWeaver.isInstrumentationAvailable();
	}

}