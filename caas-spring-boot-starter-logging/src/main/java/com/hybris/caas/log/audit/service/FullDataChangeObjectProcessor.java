package com.hybris.caas.log.audit.service;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.hybris.caas.log.util.JsonNodeUtils;

/**
 * Append a single value/attribute to the audit message named "value" which
 * contains a full serialized copy of the old object and of the new object.
 */
public class FullDataChangeObjectProcessor implements DataChangeObjectProcessor
{
	private static final String VALUE = "value";

	@Override
	public void process(DataChangeObject dataChangeObject, DataChangeValueAppender appenderFunction)
	{
		final JsonNode nonNullOldObject = Optional.ofNullable(dataChangeObject).map(DataChangeObject::getOldObject).orElse(JsonNodeUtils.valueToTree(new Object()));
		final JsonNode nonNullNewObject = Optional.ofNullable(dataChangeObject).map(DataChangeObject::getNewObject).orElse(JsonNodeUtils.valueToTree(new Object()));

		try
		{
			appenderFunction.appendValues(VALUE, JsonNodeUtils.getMapper().writeValueAsString(nonNullOldObject),
					JsonNodeUtils.getMapper().writeValueAsString(nonNullNewObject));
		}
		catch (final JsonProcessingException e)
		{
			throw new AuditLoggingException("Error serializing audit message data changes.", e);
		}
	}

}
