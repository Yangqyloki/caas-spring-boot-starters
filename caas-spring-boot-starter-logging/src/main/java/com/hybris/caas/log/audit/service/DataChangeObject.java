package com.hybris.caas.log.audit.service;

import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.hybris.caas.log.util.JsonNodeUtils;

/**
 * Data transfer object to be used when audit logging configuration changes audit messages and data modifications audit messages.
 * To be used to contain the information about the old and new objects.
 * <p>
 * Update Usage:
 * <pre>
 * DataChangeObject dataChangeObject = DataChangeObject.withOldValue(myOldObject);
 *
 * myOldObject.setValue1("newValue1");
 * myOldObject.setValue2("newValue2");
 *
 * dataChangeObject.setNewValue(myOldObject);
 * </pre>
 * Create Usage
 * <pre>
 * MyObject myNewObject = new MyObject();
 * myNewObject.setValue1("newValue1");
 * myNewObject.setValue2("newValue2");
 *
 * DataChangeObject dataChangeObject = DataChangeObject.withNewValue(myNewObject);
 * </pre>
 * Delete Usage
 * <pre>
 * MyObject myOldObject = myObjectService.getById(myId);
 * DataChangeObject dataChangeObject = DataChangeObject.withOldValue(myOldValue);
 * </pre>
 */
public final class DataChangeObject
{
	private JsonNode oldObject = null;
	private JsonNode newObject = null;

	DataChangeObject(final JsonNode oldObject, final JsonNode newObject)
	{
		this.oldObject = oldObject;
		this.newObject = newObject;
	}

	public DataChangeObject setNewValue(final Object newValue)
	{
		Assert.notNull(newValue, "newValue cannot be null");
		this.newObject = JsonNodeUtils.valueToTree(newValue);
		return this;
	}

	public JsonNode getOldObject()
	{
		return oldObject;
	}

	public JsonNode getNewObject()
	{
		return newObject;
	}

	public static DataChangeObject withOldValue(final @Nullable Object oldValue)
	{
		final JsonNode jsonNode = Objects.isNull(oldValue) ? JsonNodeUtils.getMapper().createObjectNode() : JsonNodeUtils.valueToTree(oldValue);
		return new DataChangeObject(jsonNode, null);
	}

	public static DataChangeObject withNewValue(final @Nullable Object newValue)
	{
		return new DataChangeObject(null, JsonNodeUtils.valueToTree(newValue));
	}

}
