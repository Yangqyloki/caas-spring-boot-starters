package com.hybris.caas.multitenant.web.util;

import com.hybris.caas.error.exception.BusinessException;
import com.hybris.caas.multitenant.service.TenantService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Implementation of {@link TenantLock} based on {@link Map} having the tenant string as key and {@link ReentrantLock} used for lock.
 */
public class TenantLockImpl implements TenantLock
{
	private final Map<String, Lock> tenantLockMap = new ConcurrentHashMap<>();

	private TenantService tenantService;
	private BusinessException failedLockingException;

	public TenantLockImpl(final TenantService tenantService, final BusinessException failedLockingException)
	{
		this.tenantService = tenantService;
		this.failedLockingException = failedLockingException;
	}

	/**
	 * Gets the exception to be thrown when the lock cannot be acquired.
	 *
	 * @return the exception to be thrown when the lock cannot be acquired
	 */
	public BusinessException getFailedLockingException()
	{
		return failedLockingException;
	}

	@Override
	public <T> T withLock(final Supplier<T> supplier)
	{
		boolean lockAcquired = false;
		final Lock tenantLock = tenantLockMap.computeIfAbsent(tenantService.getTenant(), k -> new ReentrantLock());

		try
		{
			lockAcquired = tenantLock.tryLock();
			if (lockAcquired)
			{
				return supplier.get();
			}
			else
			{
				throw failedLockingException;
			}
		}
		finally
		{
			if (lockAcquired)
			{
				tenantLock.unlock();
			}
		}
	}
}
