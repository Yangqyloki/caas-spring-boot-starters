package com.hybris.caas.error.converter.spring;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;

/**
 * Convert a {@link MultipartException} to a {@link ErrorMessage}.
 */
public class MultipartExceptionConverter extends AbstractExceptionConverter<MultipartException>
{
	static final String MESSAGE = "Multipart request resolution failed. Please ensure that each part is properly formatted.";

	@Override
	protected ErrorMessage convert(MultipartException ex)
	{
		return ErrorMessage.builder()
				.withMessage(MESSAGE)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_MULTIPART_RESOLUTION_ERROR)
				.build();
	}
}