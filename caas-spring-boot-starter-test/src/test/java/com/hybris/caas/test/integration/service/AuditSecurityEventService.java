package com.hybris.caas.test.integration.service;

import com.hybris.caas.log.audit.annotation.AuditSecurityEvent;
import com.hybris.caas.test.integration.model.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuditSecurityEventService
{
	@AuditSecurityEvent
	public AuditEntity trigger()
	{
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
