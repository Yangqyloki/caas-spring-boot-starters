package com.hybris.caas.multitenant;

import com.hybris.caas.multitenant.jpa.aspect.TransactionTenantSetterAspect;

import javax.persistence.EntityManager;
import java.util.Objects;

/**
 * Holds the tenant identifier associated with a request.
 * <p>
 * Please see {@link TransactionTenantSetterAspect} for more details about the position of a parameter of this type
 * for a method that requires the tenant to be set for the {@link EntityManager} within a transaction.
 */
public class TenantHolder
{
	private final String tenant;

	public TenantHolder(final String tenant)
	{
		this.tenant = tenant;
	}

	public String getTenant()
	{
		return tenant;
	}

	@Override
	public String toString()
	{
		return tenant;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		final TenantHolder that = (TenantHolder) o;
		return Objects.equals(tenant, that.tenant);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(tenant);
	}

	public static TenantHolder of(final String tenant)
	{
		return new TenantHolder(tenant);
	}
}
