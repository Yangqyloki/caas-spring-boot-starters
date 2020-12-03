package com.hybris.caas.error.db;

import java.util.Optional;

/**
 * Defines methods for mapping a {@link Throwable} to a context relevant error message.
 */
public interface ExceptionToMessageMapper<T extends Throwable>
{
	/**
	 * Maps a {@link Throwable} to a context relevant error message.
	 *
	 * @param exception the exception to map
	 * @return the context relevant error message
	 */
	Optional<String> map(T exception);
}
