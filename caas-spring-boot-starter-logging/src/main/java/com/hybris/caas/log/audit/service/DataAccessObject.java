package com.hybris.caas.log.audit.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object to be used when audit logging data access audit messages.
 * To be used to contain information about attributes and/or attachment that have been read successfully or not.
 * <p>
 * Example Usage:
 * <pre>
 * DataAccessObject.build()
 * 		.addAttributes(true, "firstName", "lastName", "email")
 * 		.addAttributes(false, "shippingAddress");
 * <pre>
 */
public final class DataAccessObject
{
	final private Map<String, Boolean> attributes;
	final private Map<String, String> attachments;

	DataAccessObject()
	{
		attributes = new HashMap<>();
		attachments = new HashMap<>();
	}

	public static DataAccessObject build()
	{
		return new DataAccessObject();
	}

	public DataAccessObject addAttributes(final boolean read, final String... names)
	{
		Arrays.asList(names).forEach(name -> attributes.put(name, read));
		return this;
	}

	public DataAccessObject addAttachment(final String id, final String name)
	{
		attachments.put(id, name);
		return this;
	}

	Map<String, Boolean> getAttributes()
	{
		return attributes;
	}

	Map<String, String> getAttachments()
	{
		return attachments;
	}
}
