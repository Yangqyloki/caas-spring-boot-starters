package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.InvalidFieldException;
import org.springframework.core.NestedExceptionUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_JSON_PARSE_FAILED;

public abstract class AbstractJacksonExceptionConverter<T extends JsonMappingException> extends AbstractExceptionConverter<T>
{
	protected static final String JSON_PARSE_EXCEPTION_MORE_INFO = "Please ensure payload conforms to API specification. Error at [line: %s, column: %s].";

	/**
	 * Retrieves the structural path of the problematic property using JsonMappingException.Reference list.
	 *
	 * @param ex the JsonMappingException
	 * @return the field name that causes the JsonMappingException
	 */
	protected String retrieveFieldName(final T ex)
	{
		return Optional.ofNullable(ex.getPath())
				.orElseGet(Collections::emptyList)
				.stream()
				.map(this::extractReferenceValue)
				.collect(Collectors.joining(".")) + findFieldNameRootCause(ex);
	}

	/**
	 * Builds the message that could be used for setting more info field of the error object with details about the location of the error.
	 *
	 * @param location the {@link JsonLocation} providing details about the location of the error (line, column)
	 * @return the message that could be used for setting the more info field of the error object
	 */
	protected String buildLocationMessage(final JsonLocation location)
	{
		return Objects.nonNull(location) ?
				String.format(JSON_PARSE_EXCEPTION_MORE_INFO, location.getLineNr(), location.getColumnNr()) :
				null;
	}

	protected String buildDetailedDescription(final T ex)
	{
		final Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
		if (cause instanceof InvalidFieldException)
		{
			return cause.getMessage();
		}
		return EXCEPTION_MESSAGE_JSON_PARSE_FAILED;
	}

	/**
	 * Extract property name or index from JsonMappingException.Reference.
	 *
	 * @param reference the element reference from a reference path
	 * @return the value or index found in the reference.
	 */
	private String extractReferenceValue(JsonMappingException.Reference reference)
	{
		if (Objects.nonNull(reference.getFieldName()))
		{
			return reference.getFieldName();
		}
		else if (reference.getIndex() > -1)
		{
			return "[" + reference.getIndex() + "]";
		}
		return "[Unknown]";
	}

	/**
	 * Find root cause field name.
	 *
	 * @param exception the exception to find root cause.
	 * @return the name of the field identified in the root cause.
	 */
	private String findFieldNameRootCause(final T exception)
	{
		final Throwable cause = NestedExceptionUtils.getMostSpecificCause(exception);
		if (cause instanceof InvalidFieldException)
		{
			return "." + ((InvalidFieldException) cause).getFieldName();
		}
		return "";
	}
}
