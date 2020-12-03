package com.hybris.caas.log.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hybris.caas.log.config.AuditLogProperties;
import com.hybris.caas.log.context.UserProvider;
import com.hybris.caas.log.util.JsonNodeUtils;
import com.sap.xs.audit.api.AuditLogMessage;
import com.sap.xs.audit.api.TransactionalAuditLogMessage;
import com.sap.xs.audit.api.exception.AuditLogNotAvailableException;
import com.sap.xs.audit.api.exception.AuditLogWriteException;
import com.sap.xs.audit.api.v2.AuditLogMessageFactory;
import com.sap.xs.audit.api.v2.AuditedDataSubject;
import com.sap.xs.audit.api.v2.AuditedObject;
import com.sap.xs.audit.api.v2.ConfigurationChangeAuditMessage;
import com.sap.xs.audit.api.v2.DataAccessAuditMessage;
import com.sap.xs.audit.api.v2.DataModificationAuditMessage;
import com.sap.xs.audit.api.v2.SecurityEventAuditMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Optional;

import static com.hybris.caas.log.audit.service.Constants.AuditedDataSubject.Identifier.ACCOUNT_ID;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.AuditedObjectType.ONLINE_SYSTEM;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Identifier.MODULE;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Identifier.NAME;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Identifier.OBJECT_ID;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Identifier.OBJECT_TYPE;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Name.SAP_HYBRIS_CAAS;
import static com.hybris.caas.log.audit.service.Constants.Channel.WEB_SERVICE;

/**
 * Audit logger implementation that will leverage the
 * {@link AuditLogMessageFactory} to log the messages. When the application
 * using this logger has enabled spring asynchronous processing via
 * {@link EnableAsync}, then all audit logging will be done by another thread.
 * <p>
 * Audit logging is done through the use of {@link TransactionalAuditLogMessage#logSuccess()}.
 *
 * @see EnableAsync
 * @see Async
 */
@Async
public class AsyncAuditLogger implements AuditLogger
{
	private final AuditLogProperties auditLogProperties;
	private final AuditLogMessageFactory auditLogMessageFactory;
	private final DataChangeObjectProcessor dataChangeObjectProcessor;
	private final UserProvider userProvider;

	public AsyncAuditLogger(final AuditLogProperties auditLogProperties, final AuditLogMessageFactory auditLogMessageFactory,
			final DataChangeObjectProcessor dataChangeObjectProcessor, final UserProvider userProvider)
	{
		this.auditLogProperties = auditLogProperties;
		this.auditLogMessageFactory = auditLogMessageFactory;
		this.dataChangeObjectProcessor = dataChangeObjectProcessor;
		this.userProvider = userProvider;
	}

	@Override
	public void logConfigurationChangeAuditMessage(final String objectId, final String objectType, final DataChangeObject dataChangeObject)
	{
		final AuditedObject auditedObject = createAuditedObject(auditLogProperties.getServiceName(), objectType, objectId);

		final ConfigurationChangeAuditMessage configurationChangeAuditMessage = auditLogMessageFactory.createConfigurationChangeAuditMessage();
		configurationChangeAuditMessage.setUser(userProvider.getUserId());
		configurationChangeAuditMessage.setTenant(userProvider.getSubaccountId());
		configurationChangeAuditMessage.setObject(auditedObject);

		dataChangeObjectProcessor.process(dataChangeObject,
				(name, oldValue, newValue) -> configurationChangeAuditMessage.addValue(name, oldValue, newValue));
		log(configurationChangeAuditMessage, "configuration change");
	}

	@Override
	public void logSecurityEventAuditMessage(final Object securityEventData)
	{
		final SecurityEventAuditMessage securityEventAuditMessage = auditLogMessageFactory.createSecurityEventAuditMessage();
		securityEventAuditMessage.setUser(userProvider.getUserId());
		securityEventAuditMessage.setTenant(userProvider.getSubaccountId());
		securityEventAuditMessage.setIp(userProvider.getClientIp());

		try
		{
			final String securityEventString = JsonNodeUtils.getMapper().writeValueAsString(Optional.ofNullable(securityEventData).orElse(new Object()));
			securityEventAuditMessage.setData(securityEventString);
		}
		catch (final JsonProcessingException e)
		{
			throw new AuditLoggingException("Error writing security event audit message.", e);
		}
		log(securityEventAuditMessage, "security event");
	}

	@Override
	public void logDataAccessAuditMessage(final String objectId, final String objectType, final DataAccessObject object, final DataSubject dataSubject)
	{
		final DataAccessAuditMessage dataAccessAuditMessage = auditLogMessageFactory.createDataAccessAuditMessage();
		dataAccessAuditMessage.setUser(userProvider.getUserId());
		dataAccessAuditMessage.setTenant(userProvider.getSubaccountId());
		dataAccessAuditMessage.setChannel(WEB_SERVICE);

		dataAccessAuditMessage.setObject(createAuditedObject(auditLogProperties.getServiceName(), objectType, objectId));
		dataAccessAuditMessage.setDataSubject(createAuditedDataSubject(dataSubject));

		object.getAttributes().entrySet().forEach(entry -> dataAccessAuditMessage.addAttribute(entry.getKey(), entry.getValue()));
		object.getAttachments().entrySet().forEach(entry -> dataAccessAuditMessage.addAttachment(entry.getKey(), entry.getValue()));

		log(dataAccessAuditMessage, "data access");
	}

	@Override
	public void logDataAccessAuditMessage(final String objectId, final Object object, final boolean read, final DataSubject dataSubject)
	{
		final List<String> attrList = JsonNodeUtils.getKeys(JsonNodeUtils.valueToTree(object));

		DataAccessObject dataAccessObject = DataAccessObject.build().addAttributes(read, attrList.toArray(new String[attrList.size()]));
		logDataAccessAuditMessage(objectId, object.getClass().getSimpleName(), dataAccessObject, dataSubject);
	}

	@Override
	public void logDataModificationAuditMessage(String objectId, String objectType, DataChangeObject dataChangeObject, DataSubject dataSubject)
	{
		final DataModificationAuditMessage dataModificationAuditMessage = auditLogMessageFactory.createDataModificationAuditMessage();
		dataModificationAuditMessage.setUser(userProvider.getUserId());
		dataModificationAuditMessage.setTenant(userProvider.getSubaccountId());

		dataModificationAuditMessage.setObject(createAuditedObject(auditLogProperties.getServiceName(), objectType, objectId));
		dataModificationAuditMessage.setDataSubject(createAuditedDataSubject(dataSubject));

		dataChangeObjectProcessor.process(dataChangeObject,
				(name, oldValue, newValue) -> dataModificationAuditMessage.addAttribute(name, oldValue, newValue));
		log(dataModificationAuditMessage, "data modification");
	}

	/**
	 * Perform the actual audit logging. Catch checked exceptions and convert them to runtime exception.
	 * This will only rethrow the runtime exception if the actual audit backing service is enabled.
	 *
	 * @param auditLogMessage the audit log message
	 * @param operation the name of the operation being performed.
	 */
	private <T extends AuditLogMessage> void log(final T auditLogMessage, final String operation)
	{
		try
		{
			auditLogMessage.log();
		}
		catch (AuditLogNotAvailableException | AuditLogWriteException e)
		{
			if (auditLogProperties.isEnabled())
			{
				throw new AuditLoggingException("Unable to process audit logging for " + operation + ".", e);
			}
		}
	}

	/**
	 * Create audited object.
	 *
	 * @param serviceName the service name
	 * @param objectType  the type of the object containing the configuration changes
	 * @param objectId    the identifier of the object containing the configuration changes
	 * @return the audited object
	 */
	private AuditedObject createAuditedObject(final String serviceName, final String objectType, final String objectId)
	{
		final AuditedObject auditedObject = auditLogMessageFactory.createAuditedObject();
		auditedObject.setType(ONLINE_SYSTEM);
		auditedObject.addIdentifier(NAME, SAP_HYBRIS_CAAS);
		auditedObject.addIdentifier(MODULE, serviceName);
		auditedObject.addIdentifier(OBJECT_TYPE, objectType);
		auditedObject.addIdentifier(OBJECT_ID, objectId);
		return auditedObject;
	}

	/**
	 * Create audited data subject.
	 *
	 * @param dataSubject data transfer object containing the data subject's identifier and role
	 * @return the audited data subject
	 */
	private AuditedDataSubject createAuditedDataSubject(final DataSubject dataSubject)
	{
		final AuditedDataSubject auditedDataSubject = auditLogMessageFactory.createAuditedDataSubject();
		auditedDataSubject.addIdentifier(ACCOUNT_ID, dataSubject.getAccountId());
		auditedDataSubject.setRole(dataSubject.getRole());
		auditedDataSubject.setType(dataSubject.getRole());
		return auditedDataSubject;
	}

}
