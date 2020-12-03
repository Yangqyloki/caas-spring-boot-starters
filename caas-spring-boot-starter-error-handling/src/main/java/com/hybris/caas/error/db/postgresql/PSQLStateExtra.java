package com.hybris.caas.error.db.postgresql;

import org.postgresql.util.PSQLState;

/**
 * Defines extra SQLState codes that are not covered by {@link PSQLState}.
 */
public enum PSQLStateExtra
{
	UNIQUE_VIOLATION("23505"), FOREIGN_KEY_VIOLATION("23503");

	private final String state;

	PSQLStateExtra(String state)
	{
		this.state = state;
	}

	public String getState()
	{
		return state;
	}
}
