package com.hybris.caas.data.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base Configuration to include starters package in jpa base packages while components scanning
 */
public abstract class CaaSJpaBaseConfiguration extends JpaBaseConfiguration
{
	private static final String CAAS_STARTERS_JPA_BASE_PACKAGE = "com.hybris.caas.data.persistence";

	protected CaaSJpaBaseConfiguration(final DataSource dataSource, final JpaProperties properties,
			final ObjectProvider<JtaTransactionManager> jtaTransactionManager)
	{
		super(dataSource, properties, jtaTransactionManager);
	}

	@Override
	protected String[] getPackagesToScan()
	{
		final List<String> packages = Arrays.stream(super.getPackagesToScan()).collect(Collectors.toList());
		packages.add(CAAS_STARTERS_JPA_BASE_PACKAGE);
		return StringUtils.toStringArray(packages);
	}

}
