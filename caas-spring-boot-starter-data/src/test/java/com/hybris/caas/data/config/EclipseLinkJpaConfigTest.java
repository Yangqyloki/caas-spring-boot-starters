package com.hybris.caas.data.config;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EclipseLinkJpaConfigTest
{
	@Mock
	private DataSource dataSource;

	@Mock
	private JpaProperties jpaProperties;

	@Mock
	private ObjectProvider<JtaTransactionManager> jtaTransactionManagerObjectProvider;

	@Mock
	private ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizersObjectProvider;

	private EclipseLinkJpaConfig jpaConfig;

	@BeforeEach
	public void setUp()
	{
		jpaConfig = new EclipseLinkJpaConfig(dataSource, jpaProperties, jtaTransactionManagerObjectProvider);
	}

	@Test
	public void should_use_EclipseLink_jpa_adapter()
	{
		assertThat(jpaConfig.createJpaVendorAdapter(), instanceOf(EclipseLinkJpaVendorAdapter.class));
	}

	@Test
	public void should_only_set_weaving_vendor_property_to_static()
	{
		final Map<String, Object> vendorProperties = jpaConfig.getVendorProperties();

		assertThat(vendorProperties.values(), hasSize(1));
		assertThat(vendorProperties, hasEntry(PersistenceUnitProperties.WEAVING, "static"));
	}

	@Test
	public void should_only_set_weaving_vendor_property_to_true_when_instrumentation_available()
	{
		final EclipseLinkJpaConfig jpaConfigSpy = Mockito.spy(jpaConfig);
		when(jpaConfigSpy.detectWeavingMode()).thenReturn(Boolean.TRUE);

		final Map<String, Object> vendorProperties = jpaConfigSpy.getVendorProperties();

		assertThat(vendorProperties.values(), hasSize(1));
		assertThat(vendorProperties, hasEntry(PersistenceUnitProperties.WEAVING, "true"));
	}
}
