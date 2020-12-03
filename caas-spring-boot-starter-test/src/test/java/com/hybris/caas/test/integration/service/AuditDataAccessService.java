package com.hybris.caas.test.integration.service;

import com.hybris.caas.log.audit.service.AuditLogger;
import com.hybris.caas.log.audit.service.DataAccessObject;
import com.hybris.caas.log.audit.service.DataSubject;
import com.hybris.caas.log.audit.service.DataSubject.Role;
import com.hybris.caas.test.integration.model.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuditDataAccessService
{
	private final AuditLogger auditLogger;

	public AuditDataAccessService(final AuditLogger auditLogger)
	{
		this.auditLogger = auditLogger;
	}

	public AuditEntity dataAccess()
	{
		auditLogger.logDataAccessAuditMessage("abc123", "AuditEntity",
				DataAccessObject.build().addAttributes(true, "number", "string", "listString", "subEntity.subString", "subEntity.subNumber").addAttributes(false, "id"),
				DataSubject.of("zxy987", Role.MERCHANT));

		final AuditEntity.TestSubEntity subEntity = new AuditEntity.TestSubEntity();
		subEntity.setSubNumber(59);
		subEntity.setSubString("SUB FOO BAR");

		final AuditEntity entity = new AuditEntity();
		entity.setId("abc123");
		entity.setNumber(17);
		entity.setString("FOO BAR");
		entity.setListStrings(Arrays.asList("WORLD", "HELLO"));
		entity.setSubEntity(subEntity);

		return entity;
	}
}
