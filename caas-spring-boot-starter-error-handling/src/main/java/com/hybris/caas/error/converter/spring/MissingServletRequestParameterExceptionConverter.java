package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;

/**
 * Convert a {@link MissingServletRequestParameterException} to a {@link ErrorMessage}.
 */
public class MissingServletRequestParameterExceptionConverter extends AbstractExceptionConverter<MissingServletRequestParameterException>
{
	protected static final String EXCEPTION_MESSAGE = "Missing query parameter.";

	@Override
	protected ErrorMessage convert(MissingServletRequestParameterException ex)
	{
		final ErrorMessageDetail detail = ErrorMessageDetail.builder()
				.withField(ex.getParameterName())
				.withMessage(ex.getMessage())
				.withType(ErrorConstants.SUB_TYPE_400_MISSING_QUERY_PARAMETER)
				.build();

		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.withMoreInfo(ErrorConstants.INFO)
				.addDetails(detail).build();
	}
}
