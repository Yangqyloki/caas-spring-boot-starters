package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.UpdateConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map.Entry;
import java.util.Objects;

/**
 * Convert a {@link UpdateConflictException} to a {@link ErrorMessage}.
 */
@Component
public class UpdateConflictExceptionConverter extends AbstractExceptionConverter<UpdateConflictException>
{
	private static final Logger LOG = LoggerFactory.getLogger(UpdateConflictExceptionConverter.class);

	protected static final String UNMODIFIABLE_FIELD = "unmodifiable_field";
	protected static final String MESSAGE = "The requested update cannot be performed since it would place the resource in a conflicting state.";
	protected static final String DETAIL_MESSAGE = "Cannot change the field from '%s' to '%s'";

	@Override
	protected ErrorMessage convert(final UpdateConflictException ex)
	{
		final ErrorMessage.Builder errorMessageBuilder = ErrorMessage.builder()
				.withMessage(MESSAGE)
				.withStatus(HttpStatus.CONFLICT.value())
				.withType(ErrorConstants.TYPE_409_CONFLICT_RESOURCE)
				.withMoreInfo(ErrorConstants.INFO);

		ex.getConflicts().entrySet().forEach(violation -> errorMessageBuilder.addDetails(buildDetail(violation)));

		return errorMessageBuilder.build();
	}

	private static ErrorMessageDetail buildDetail(final Entry<String, Object[]> violation)
	{
		final String string1 = getValueAtIndex(0, violation.getValue());
		final String string2 = getValueAtIndex(1, violation.getValue());

		return ErrorMessageDetail.builder()
				.withMessage(String.format(DETAIL_MESSAGE, string1, string2))
				.withType(UNMODIFIABLE_FIELD)
				.withField(violation.getKey())
				.build();
	}

	/**
	 * Null-safe way to get a string value at the index of the object array.
	 *
	 * @param index index of the object to read
	 * @param objects the object array
	 * @return string value
	 */
	private static String getValueAtIndex(final int index, final Object[] objects)
	{
		Object value;
		try
		{
			value = objects[index];
		}
		catch (final IndexOutOfBoundsException e)
		{
			LOG.debug("Invalid update conflict violation configuration. You should always supply 2 values in the object array: the current value and the user provided value.", e);
			value = null;
		}
		return (Objects.isNull(value)) ? null : value.toString();
	}
}
