package com.hybris.caas.multitenant.web.util;

import java.util.function.Supplier;

/**
 * Locking mechanism that only allows a single execution of the {@link Supplier} at a time per tenant.
 */
public interface TenantLock
{
	/*
	 * Tries to acquire a lock for the execution of the {@link Supplier}.
	 * If the lock was acquired, it executes the provided {@link Supplier} while holding the lock and releasing it afterwards.
	 * If the lock cannot be acquired, it throws a {@link com.hybris.caas.application.service.exception.BusinessException}.
	 *
	 * @param supplier the {@link Supplier} to be invoked while holding the lock
	 * @param <T> the type of the result returned by {@link Supplier}
	 * @return the result of invoking the provided {@link Supplier}
	 */
	<T> T withLock(Supplier<T> supplier);
}
