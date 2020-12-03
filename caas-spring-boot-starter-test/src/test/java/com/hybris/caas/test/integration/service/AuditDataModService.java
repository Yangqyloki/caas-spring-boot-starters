package com.hybris.caas.test.integration.service;

import com.hybris.caas.log.audit.service.AuditLogger;
import com.hybris.caas.log.audit.service.DataChangeObject;
import com.hybris.caas.log.audit.service.DataSubject;
import com.hybris.caas.log.audit.service.DataSubject.Role;
import com.hybris.caas.test.integration.model.AuditDto;
import com.hybris.caas.test.integration.model.AuditEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class AuditDataModService
{
	private static final String ACCOUNT_ID = "zxy987";
	private static final String OBJECT_ID = "abc123";
	private final AuditLogger auditLogger;

	public AuditDataModService(final AuditLogger auditLogger)
	{
		this.auditLogger = auditLogger;
	}

	public AuditEntity update(AuditEntity AuditEntity, AuditDto testResource)
	{
		final DataChangeObject dataChangeObject = DataChangeObject.withOldValue(AuditEntity);

		AuditEntity.setNumber(testResource.getNumber());
		AuditEntity.setString(testResource.getString());
		AuditEntity.setListStrings(testResource.getListStrings());
		AuditEntity.getSubEntity().setSubNumber(testResource.getSubResource().getSubNumber());
		AuditEntity.getSubEntity().setSubString(testResource.getSubResource().getSubString());

		auditLogger.logDataModificationAuditMessage(OBJECT_ID, AuditEntity.getClass().getSimpleName(), dataChangeObject.setNewValue(AuditEntity),
				DataSubject.of(ACCOUNT_ID, Role.MERCHANT));
		return AuditEntity;
	}

	public AuditEntity updatePartial(AuditEntity AuditEntity, AuditDto testResource)
	{
		final DataChangeObject dataChangeObject = DataChangeObject.withOldValue(AuditEntity);

		AuditEntity.setNumber(testResource.getNumber());
		AuditEntity.setString(testResource.getString());
		AuditEntity.setListStrings(testResource.getListStrings());
		AuditEntity.getSubEntity().setSubNumber(testResource.getSubResource().getSubNumber());
		AuditEntity.getSubEntity().setSubString(testResource.getSubResource().getSubString());

		auditLogger.logDataModificationAuditMessage(OBJECT_ID, AuditEntity.getClass().getSimpleName(), dataChangeObject.setNewValue(AuditEntity),
				DataSubject.of(ACCOUNT_ID, Role.MERCHANT));
		return AuditEntity;
	}

	public AuditEntity create(AuditDto testResource)
	{
		final AuditEntity AuditEntity = new AuditEntity();
		AuditEntity.setId(OBJECT_ID);
		AuditEntity.setNumber(testResource.getNumber());
		AuditEntity.setString(testResource.getString());
		AuditEntity.setListStrings(testResource.getListStrings());

		auditLogger.logDataModificationAuditMessage(OBJECT_ID, AuditEntity.getClass().getSimpleName(), DataChangeObject.withNewValue(AuditEntity),
				DataSubject.of(ACCOUNT_ID, Role.MERCHANT));
		return AuditEntity;
	}

	@Transactional
	public void delete(AuditEntity AuditEntity)
	{
		if ("throwIt".equals(AuditEntity.getId()))
		{
			throw new IllegalArgumentException();
		}
		auditLogger.logDataModificationAuditMessage(OBJECT_ID, AuditEntity.getClass().getSimpleName(), DataChangeObject.withOldValue(AuditEntity),
				DataSubject.of(ACCOUNT_ID, Role.MERCHANT));
	}
}
