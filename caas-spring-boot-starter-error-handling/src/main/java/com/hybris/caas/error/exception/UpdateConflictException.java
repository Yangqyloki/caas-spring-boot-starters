package com.hybris.caas.error.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception to be thrown when an update is attempting to update unmodifiable attributes.
 */
public class UpdateConflictException extends RuntimeException
{
	private static final long serialVersionUID = -8075002591636477450L;

	// Use of HashMap here is required, since Map interface is not Serializable.
	private final transient HashMap<String, Object[]> conflicts;

	public UpdateConflictException(final Map<String, Object[]> conflicts)
	{
		this.conflicts = new HashMap<>(conflicts);
	}

	public Map<String, Object[]> getConflicts()
	{
		return conflicts;
	}

}
