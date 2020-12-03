package com.hybris.caas.log.audit.service;

import org.springframework.lang.Nullable;

/**
 * Allow to easily do audit logging in a single line of code.
 */
public interface AuditLogger
{
	/**
	 * Log a configuration change audit message.
	 *
	 * @param objectId   the identifier of the object being audited
	 * @param objectType the type of object being audited
	 * @param dataChangeObject the old and new values of the object bing audited
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logConfigurationChangeAuditMessage(String objectId, String objectType, DataChangeObject dataChangeObject);

	/**
	 * Log a security event audit message.
	 *
	 * @param securityEventData the security event data to write to the audit log
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logSecurityEventAuditMessage(@Nullable Object securityEventData);

	/**
	 * Log a data access audit message.
	 *
	 * @param objectId    the identifier of the object being audited
	 * @param objectType  the type of the object being audited
	 * @param object      the attributes and/or attachments being accessed
	 * @param dataSubject the owner of the data being accessed
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logDataAccessAuditMessage(String objectId, String objectType, DataAccessObject object, DataSubject dataSubject);

	/**
	 * Logs a data access audit message.
	 * The object type is inferred from the {@code DataAccessObject}
	 *
	 * @param objectId    the identifier of the object being audited
	 * @param object      the object being accessed
	 * @param read        boolean indicating whether the object was successfully read or not.
	 * @param dataSubject the owner of the data being accessed
	 */
	void logDataAccessAuditMessage(final String objectId, final Object object, final boolean read, final DataSubject dataSubject);

	/**
	 * Log a data modification message.
	 *
	 * @param objectId         the identifier of the object being audited
	 * @param objectType       the type of the object being audited
	 * @param dataChangeObject the old and new values of the object bing audited
	 * @param dataSubject      the owner of the data being accessed
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logDataModificationAuditMessage(String objectId, String objectType, DataChangeObject dataChangeObject,
			DataSubject dataSubject);
}
