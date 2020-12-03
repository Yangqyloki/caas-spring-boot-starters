package com.hybris.caas.test.integration.service;

import com.hybris.caas.log.audit.annotation.AuditConfigurationChange;
import com.hybris.caas.test.integration.model.AuditDto;
import com.hybris.caas.test.integration.model.AuditEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;

@Service
public class AuditConfigurationService
{
	private static final String OBJECT_ID = "abc123";

	public AuditEntity get(String id)
	{
		final AuditEntity.TestSubEntity subEntity = new AuditEntity.TestSubEntity();
		subEntity.setSubNumber(59);
		subEntity.setSubString("SUB FOO BAR");

		final AuditEntity entity = new AuditEntity();
		entity.setId(id);
		entity.setNumber(17);
		entity.setString("FOO BAR");
		entity.setListStrings(Arrays.asList("WORLD", "HELLO"));
		entity.setSubEntity(subEntity);

		return entity;
	}

	@AuditConfigurationChange(oldValue = "AuditEntity")
	public AuditEntity update(AuditEntity AuditEntity, AuditDto testResource)
	{
		AuditEntity.setNumber(testResource.getNumber());
		AuditEntity.setString(testResource.getString());
		AuditEntity.setListStrings(testResource.getListStrings());
		AuditEntity.getSubEntity().setSubNumber(testResource.getSubResource().getSubNumber());
		AuditEntity.getSubEntity().setSubString(testResource.getSubResource().getSubString());
		return AuditEntity;
	}

	@AuditConfigurationChange(oldValue = "AuditEntity")
	public AuditEntity updatePartial(AuditEntity AuditEntity, AuditDto testResource)
	{
		AuditEntity.setNumber(testResource.getNumber());
		AuditEntity.setString(testResource.getString());
		AuditEntity.setListStrings(testResource.getListStrings());
		AuditEntity.getSubEntity().setSubNumber(testResource.getSubResource().getSubNumber());
		AuditEntity.getSubEntity().setSubString(testResource.getSubResource().getSubString());
		return AuditEntity;
	}

	@AuditConfigurationChange()
	public AuditEntity create(AuditDto testResource)
	{
		final AuditEntity entity = new AuditEntity();
		entity.setId(OBJECT_ID);
		entity.setNumber(testResource.getNumber());
		entity.setString(testResource.getString());
		entity.setListStrings(testResource.getListStrings());
		return entity;
	}

	@AuditConfigurationChange(oldValue = "AuditEntity")
	@Transactional
	public void delete(AuditEntity AuditEntity)
	{
		if ("throwIt".equals(AuditEntity.getId()))
		{
			throw new IllegalArgumentException();
		}
	}
}
