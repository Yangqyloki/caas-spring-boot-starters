package com.hybris.caas.multitenant.jpa.aspect;

import com.hybris.caas.multitenant.TenantHolder;
import com.hybris.caas.multitenant.jpa.EntityManagerHolder;
import com.hybris.caas.multitenant.jpa.annotation.TenantSetter;
import com.hybris.caas.multitenant.service.config.TenantProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * Aspect that sets the tenant for methods within a type annotated with {@link TenantSetter}
 * or directly annotated with {@link TenantSetter} and also annotated with {@link Transactional}.
 * <p>
 * Besides these annotations the method targeted by this aspect should have the {@link TenantHolder} as the first parameter
 * and the annotated type or the type providing the annotated method should implement the {@link EntityManagerHolder}
 * interface that would provide access to the {@link EntityManager} instance on which the tenant should be set.
 * <p>
 * Setting of the tenant needs to be done after the transactions has been started.
 */

@Aspect
@Component
@Order(200)
public class TransactionTenantSetterAspect
{
	private final String multiTenantSessionProperty;

	public TransactionTenantSetterAspect(final TenantProperties tenantProperties)
	{
		this.multiTenantSessionProperty = tenantProperties.getMultiTenantSessionProperty();
	}

	@Before("(@annotation(com.hybris.caas.multitenant.jpa.annotation.TenantSetter)"
			+ " || @within(com.hybris.caas.multitenant.jpa.annotation.TenantSetter))"
			+ " && (@annotation(org.springframework.transaction.annotation.Transactional)"
					+ " || @within(org.springframework.transaction.annotation.Transactional)"
					+ " || execution(@(@org.springframework.transaction.annotation.Transactional *) * *(..)))"
			+ " && args(tenantHolder,..) && this(com.hybris.caas.multitenant.jpa.EntityManagerHolder)")
	public void beforeTransactionalOperation(final JoinPoint jp, final TenantHolder tenantHolder)
	{
		final EntityManagerHolder entityManagerHolder = (EntityManagerHolder) jp.getThis();
		final EntityManager entityManager = entityManagerHolder.getEntityManager();

		final String currentTenant = (String) entityManager.getProperties().get(multiTenantSessionProperty);
		final String operationTenant = tenantHolder.getTenant();

		//check whether there is a conflict due to concurrent access to entity manager
		if (currentTenant != null && !currentTenant.equals(operationTenant))
		{
			throw new IllegalStateException(String.format("Resource conflict - the entity manager is already assigned to tenant %s."
					+ " It cannot be reassigned to tenant %s.", currentTenant, operationTenant));
		}

		entityManager.setProperty(multiTenantSessionProperty, operationTenant);
	}
}
