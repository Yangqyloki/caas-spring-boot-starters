package com.hybris.caas.log.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TransactionalRunnableSchedulerTest
{
	private CountDownLatch latch;

	@Before
	public void setUp()
	{
		latch = new CountDownLatch(10);

		TransactionSynchronizationManager.clear();
		TransactionSynchronizationManager.initSynchronization();
	}

	@After
	public void cleanUp()
	{
		TransactionSynchronizationManager.clear();
	}

	@Test
	public void should_register_task_for_after_commit()
	{
		TransactionSynchronizationManager.setActualTransactionActive(true);
		TransactionalRunnableScheduler.afterCommit(latch::countDown);

		assertThat(TransactionSynchronizationManager.getSynchronizations().size(), equalTo(1));
		final TransactionSynchronization transactionSynchronization = TransactionSynchronizationManager.getSynchronizations().get(0);

		transactionSynchronization.afterCommit();

		assertThat(latch.getCount(), equalTo(9L));
	}

	@Test
	public void should_execute_task_immediately()
	{
		TransactionSynchronizationManager.setActualTransactionActive(false);
		TransactionalRunnableScheduler.afterCommit(latch::countDown);

		assertThat(latch.getCount(), equalTo(9L));
	}
}
