package com.hybris.caas.log.audit.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTransactionalDecoratorTest
{
	@Mock
	private AuditLogger auditLogger;

	private CountDownLatch latch;
	private AuditLoggerTransactionalDecorator auditLoggerTransactionalDecorator;

	@Before
	public void setUp()
	{
		latch = new CountDownLatch(10);

		TransactionSynchronizationManager.clear();
		TransactionSynchronizationManager.initSynchronization();

		auditLoggerTransactionalDecorator = new AuditLoggerTransactionalDecorator(auditLogger);

		doAnswer(invocation -> {
			latch.countDown();
			return null;
		}).when(auditLogger).logConfigurationChangeAuditMessage(any(), any(), any());

		doAnswer(invocation -> {
			latch.countDown();
			return null;
		}).when(auditLogger).logSecurityEventAuditMessage(any());

		doAnswer(invocation -> {
			latch.countDown();
			return null;
		}).when(auditLogger).logDataAccessAuditMessage(any(), any(), any(), any());

		doAnswer(invocation -> {
			latch.countDown();
			return null;
		}).when(auditLogger).logDataModificationAuditMessage(any(), any(), any(), any());
	}

	@After
	public void cleanUp()
	{
		TransactionSynchronizationManager.clear();
	}

	@Test
	public void should_register_configuration_change_auditing_after_commit()
	{
		TransactionSynchronizationManager.setActualTransactionActive(true);

		auditLoggerTransactionalDecorator.logConfigurationChangeAuditMessage(any(), any(), any());

		commitTransactionAndAssertResult();
	}

	@Test
	public void should_register_security_event_auditing_after_commit()
	{
		TransactionSynchronizationManager.setActualTransactionActive(true);

		auditLoggerTransactionalDecorator.logSecurityEventAuditMessage(any());

		commitTransactionAndAssertResult();
	}

	@Test
	public void should_register_data_access_auditing_with_dataAccessObject_after_commit()
	{
		TransactionSynchronizationManager.setActualTransactionActive(true);

		auditLoggerTransactionalDecorator.logDataAccessAuditMessage(any(), any(), any(), any());

		commitTransactionAndAssertResult();
	}

	@Test
	public void should_register_data_access_auditing_with_object_after_commit()
	{
		TransactionSynchronizationManager.setActualTransactionActive(true);

		auditLoggerTransactionalDecorator.logDataAccessAuditMessage(any(), any(), anyBoolean(), any());

		final TransactionSynchronization transactionSynchronization = TransactionSynchronizationManager.getSynchronizations().get(0);
		transactionSynchronization.afterCommit();
		assertThat(latch.getCount(), equalTo( 10L));	}

	@Test
	public void should_register_data_modification_auditing_after_commit()
	{
		TransactionSynchronizationManager.setActualTransactionActive(true);

		auditLoggerTransactionalDecorator.logDataModificationAuditMessage(any(), any(), any(), any());

		commitTransactionAndAssertResult();
	}

	private void commitTransactionAndAssertResult()
	{
		final TransactionSynchronization transactionSynchronization = TransactionSynchronizationManager.getSynchronizations().get(0);
		transactionSynchronization.afterCommit();

		assertThat(latch.getCount(), equalTo( 9L));
	}

	@Test
	public void should_audit_immediately()
	{
		TransactionSynchronizationManager.setActualTransactionActive(false);

		auditLoggerTransactionalDecorator.logConfigurationChangeAuditMessage(any(), any(), any());

		assertThat(latch.getCount(), equalTo(9L));
	}
}
