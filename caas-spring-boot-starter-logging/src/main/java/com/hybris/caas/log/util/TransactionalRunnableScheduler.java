package com.hybris.caas.log.util;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Schedules the execution of a {@link Runnable} depending on the existence of an actual transaction active for the current thread.
 */
public final class TransactionalRunnableScheduler
{
	private TransactionalRunnableScheduler()
	{
		// private constructor
	}

	/**
	 * Registers {@link Runnable} to be invoked after the transaction commit when there is an actual transaction active.
	 * Supported by Spring transaction managers.
	 * <p>
	 * If there is no active transaction, the {@link Runnable} will be invoked right away.
	 *
	 * @param runnable the {@link Runnable} to execute
	 */
	public static void afterCommit(final Runnable runnable)
	{
		if (TransactionSynchronizationManager.isActualTransactionActive())
		{
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
			{
				@Override
				public void afterCommit()
				{
					runnable.run();
				}
			});
		}
		else
		{
			runnable.run();
		}
	}
}
