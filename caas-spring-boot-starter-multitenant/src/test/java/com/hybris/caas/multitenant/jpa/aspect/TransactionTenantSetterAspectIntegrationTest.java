package com.hybris.caas.multitenant.jpa.aspect;

import com.hybris.caas.multitenant.TenantHolder;
import com.hybris.caas.multitenant.jpa.EntityManagerHolder;
import com.hybris.caas.multitenant.jpa.annotation.TenantSetter;
import com.hybris.caas.multitenant.service.config.TenantProperties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = TransactionTenantSetterAspectIntegrationTest.TestConf.class)
public class TransactionTenantSetterAspectIntegrationTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private MethodAnnotatedStub methodAnnotatedStub;
	@Autowired
	private MethodAndClassAnnotatedStub methodAndClassAnnotatedStub;
	@Autowired
	private ClassAnnotatedStub classAnnotatedStub;
	@Autowired
	private ExtendedClassAnnotatedStub extendedClassAnnotatedStub;

	@Autowired
	private EntityManager entityManager;

	private TenantHolder tenantHolder = TenantHolder.of("tenant");

	@Before
	public void setUp()
	{
		clearInvocations(entityManager);

		when(entityManager.getProperties()).thenReturn(Collections.emptyMap());
	}

	@Test
	public void shouldCallAspectWhenMethodAnnotatedWithTenantSetterAndTransactional()
	{
		methodAnnotatedStub.methodAnnotationsService(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Test
	public void shouldCallAspectWhenMethodMetaAnnotatedWithTenantSetterAndTransactional()
	{
		methodAnnotatedStub.metaMethodAnnotationsService(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Test
	public void shouldCallAspectWhenClassAndMethodAnnotatedWithTenantSetterAndTransactional()
	{
		methodAndClassAnnotatedStub.methodAndClassAnnotationsService(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Test
	public void shouldCallAspectWhenClassAndMethodMetaAnnotatedWithTenantSetterAndTransactional()
	{
		methodAndClassAnnotatedStub.metaMethodAndClassAnnotationsService(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Test
	public void shouldCallAspectWhenClassAnnotatedWithTenantSetterAndTransactional()
	{
		classAnnotatedStub.classAnnotationsService(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Test
	public void shouldCallAspectWhenClassExtendsAnnotatedTenantSetterClass()
	{
		extendedClassAnnotatedStub.extendedClassAnnotatedStub(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Test
	public void shouldCallAspectWhenClassExtendsMetaAnnotatedTenantSetterClass()
	{
		extendedClassAnnotatedStub.metaExtendedClassAnnotatedStub(tenantHolder);

		verify(entityManager).setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenantHolder.getTenant());
	}

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class TestConf
	{
		@Bean
		public TransactionTenantSetterAspect ignoreExceptionAspect()
		{
			return new TransactionTenantSetterAspect(new TenantProperties());
		}

		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}

		@Bean
		public MethodAnnotatedStub methodAnnotatatedStub()
		{
			return new MethodAnnotatedStub(entityManager());
		}

		@Bean
		public MethodAndClassAnnotatedStub methodAndClassAnnotatedStub()
		{
			return new MethodAndClassAnnotatedStub(entityManager());
		}

		@Bean
		public ExtendedClassAnnotatedStub extendedClassAnnotatedStub()
		{
			return new ExtendedClassAnnotatedStub(entityManager());
		}

		@Bean
		public ClassAnnotatedStub classAnnotatedStub()
		{
			return new ClassAnnotatedStub(entityManager());
		}

	}

	private static class BaseEntityManagerHolder implements EntityManagerHolder
	{
		private final EntityManager entityManager;

		BaseEntityManagerHolder(final EntityManager entityManager)
		{
			this.entityManager = entityManager;
		}

		@Override
		public EntityManager getEntityManager()
		{
			return entityManager;
		}
	}

	private static class MethodAnnotatedStub extends BaseEntityManagerHolder
	{
		MethodAnnotatedStub(final EntityManager entityManager)
		{
			super(entityManager);
		}

		@TenantSetter
		@Transactional
		void methodAnnotationsService(final TenantHolder tenantHolder)
		{
			// stub method
		}

		@TenantSetter
		@MetaTransactional
		void metaMethodAnnotationsService(final TenantHolder tenantHolder)
		{
			// stub method
		}
	}

	@TenantSetter
	private static class MethodAndClassAnnotatedStub extends BaseEntityManagerHolder
	{
		MethodAndClassAnnotatedStub(final EntityManager entityManager)
		{
			super(entityManager);
		}

		@Transactional
		void methodAndClassAnnotationsService(final TenantHolder tenantHolder)
		{
			// stub method
		}

		@MetaTransactional
		void metaMethodAndClassAnnotationsService(final TenantHolder tenantHolder)
		{
			// stub method
		}
	}

	@TenantSetter
	@Transactional
	private static class ClassAnnotatedStub extends BaseEntityManagerHolder
	{
		ClassAnnotatedStub(final EntityManager entityManager)
		{
			super(entityManager);
		}

		void classAnnotationsService(final TenantHolder tenantHolder)
		{
			// stub method
		}

	}

	private static class ExtendedClassAnnotatedStub extends MethodAndClassAnnotatedStub
	{
		ExtendedClassAnnotatedStub(final EntityManager entityManager)
		{
			super(entityManager);
		}

		@Transactional
		void extendedClassAnnotatedStub(final TenantHolder tenantHolder)
		{
			// stub method
		}

		@MetaTransactional
		void metaExtendedClassAnnotatedStub(final TenantHolder tenantHolder)
		{
			// stub method
		}
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Transactional
	public @interface MetaTransactional
	{
	}

}
