package com.hybris.caas.log.audit.service;

import static com.hybris.caas.log.util.TransactionalRunnableScheduler.afterCommit;

/**
 * Decorator implementation of {@link AuditLogger} that schedules the execution of the audit logging depending
 * on the existence of an actual transaction active for the current thread.
 * <p>
 * In the case of an actual transaction active, the audit logging is scheduled for execution after the transaction commits.
 * If the transaction rolls back, the audit logging does not take place.
 * <p>
 * In the case when there is no actual transaction active, audit logging is invoked right away.
 */
public class AuditLoggerTransactionalDecorator implements AuditLogger
{
	private final AuditLogger delegate;

	public AuditLoggerTransactionalDecorator(final AuditLogger delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void logConfigurationChangeAuditMessage(final String objectId, final String objectType, final DataChangeObject dataChangeObject)
	{
		afterCommit(() -> delegate.logConfigurationChangeAuditMessage(objectId, objectType, dataChangeObject));
	}

	@Override
	public void logSecurityEventAuditMessage(final Object securityEventData)
	{
		afterCommit(() -> delegate.logSecurityEventAuditMessage(securityEventData));
	}

	@Override
	public void logDataAccessAuditMessage(String objectId, String objectType, DataAccessObject object, DataSubject dataSubject)
	{
		afterCommit(() -> delegate.logDataAccessAuditMessage(objectId, objectType, object, dataSubject));
	}

	@Override
	public void logDataAccessAuditMessage(String objectId, Object object, boolean read, DataSubject dataSubject)
	{
		afterCommit(() -> delegate.logDataAccessAuditMessage(objectId, object, read, dataSubject));
	}

	@Override
	public void logDataModificationAuditMessage(String objectId, String objectType, DataChangeObject dataChangeObject, DataSubject dataSubject)
	{
		afterCommit(() -> delegate.logDataModificationAuditMessage(objectId, objectType, dataChangeObject, dataSubject));
	}
}
