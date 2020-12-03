package com.hybris.caas.error.converter.spring;

import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;

/**
 * Convert a {@link MaxUploadSizeExceededException} to a {@link ErrorMessage}.
 */
public class MaxUploadSizeExceededExceptionConverter extends AbstractExceptionConverter<MaxUploadSizeExceededException>
{
	static final String MESSAGE = "File upload exceeded the maximum permitted size of %s.";
	private final DataSize maxSize;

	public MaxUploadSizeExceededExceptionConverter(final DataSize maxSize)
	{
		this.maxSize = maxSize;
	}

	@Override
	protected ErrorMessage convert(MaxUploadSizeExceededException ex)
	{
		return ErrorMessage.builder()
				.withMessage(String.format(MESSAGE, maxSize))
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.build();
	}

	public DataSize getMaxSize()
	{
		return maxSize;
	}
}
