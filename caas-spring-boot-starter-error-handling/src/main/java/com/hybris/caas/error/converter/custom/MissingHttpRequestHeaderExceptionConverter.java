package com.hybris.caas.error.converter.custom;

import org.springframework.http.HttpStatus;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.MissingHttpRequestHeaderException;

/**
 *
 * Convert a {@link MissingHttpRequestHeaderException} to a
 * {@link ErrorMessage}.
 */
public class MissingHttpRequestHeaderExceptionConverter extends AbstractExceptionConverter<MissingHttpRequestHeaderException>
{

	@Override
	protected ErrorMessage convert(MissingHttpRequestHeaderException ex)
	{
		final ErrorMessageDetail errorMessageDetail = ErrorMessageDetail.builder()
				.withField(ex.getHeader())
				.withType(ErrorConstants.SUB_TYPE_400_MISSING_HEADER)
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
