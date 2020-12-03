package com.hybris.caas.error.converter.custom;

import org.springframework.http.HttpStatus;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;

/**
 *
 * Convert a {@link InvalidHttpRequestHeaderException} to a
 * {@link ErrorMessage}.
 */
public class InvalidHttpRequestHeaderExceptionConverter extends AbstractExceptionConverter<InvalidHttpRequestHeaderException>
{

	@Override
	protected ErrorMessage convert(InvalidHttpRequestHeaderException ex)
	{
		final ErrorMessageDetail errorMessageDetail = ErrorMessageDetail.builder()
				.withField(ex.getHeader())
				.withType(ErrorConstants.SUB_TYPE_400_INVALID_HEADER)
				.build();

		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.withMoreInfo(ErrorConstants.INFO)
				.addDetails(errorMessageDetail)
				.build();
	}

}
