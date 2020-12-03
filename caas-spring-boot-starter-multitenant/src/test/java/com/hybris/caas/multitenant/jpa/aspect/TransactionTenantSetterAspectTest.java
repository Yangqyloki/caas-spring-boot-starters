package com.hybris.caas.multitenant.jpa.aspect;

import com.hybris.caas.multitenant.TenantHolder;
import com.hybris.caas.multitenant.jpa.EntityManagerHolder;
import com.hybris.caas.multitenant.service.config.TenantProperties;
import org.aspectj.lang.JoinPoint;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionTenantSetterAspectTest
{
	private static final String TENANT_SAP = "sap";

	private final TransactionTenantSetterAspect aspect = new TransactionTenantSetterAspect(
			new TenantProperties());

	@Mock
	private JoinPoint joinPoint;

	@Mock
	private TenantHolder tenantHolder;

	@Mock
	private EntityManager entityManager;

	@Mock
	private EntityManagerHolder entityManagerHolder;

	@Before
	public void setUp()
	{
		when(tenantHolder.getTenant()).thenReturn(TENANT_SAP);
		when(joinPoint.getThis()).thenReturn(entityManagerHolder);
		when(entityManagerHolder.getEntityManager()).thenReturn(entityManager);
		when(entityManager.getProperties()).thenReturn(Collections.emptyMap());
	}

	@Test
	public void should_set_tenant_when_no_tenant_is_set()
	{
		aspect.beforeTransactionalOperation(joinPoint, tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, TENANT_SAP);
	}

	@Test
	public void should_set_tenant_when_same_tenant_is_already_set()
	{
		when(entityManager.getProperties()).thenReturn(
				Collections.singletonMap(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, TENANT_SAP));

		aspect.beforeTransactionalOperation(joinPoint, tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, TENANT_SAP);
	}

	@Test(expected = IllegalStateException.class)
	public void should_fail_when_different_tenant_is_already_set()
	{
		when(entityManager.getProperties()).thenReturn(
				Collections.singletonMap(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, "dummytenant"));

		aspect.beforeTransactionalOperation(joinPoint, tenantHolder);
	}
}
