package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convert a {@link MethodArgumentTypeMismatchException} to a {@link com.hybris.caas.error.ErrorMessage}.
 */
public class MethodArgumentTypeMismatchExceptionConverter extends AbstractExceptionConverter<MethodArgumentTypeMismatchException>
{
	protected static final String EXCEPTION_MESSAGE = "Request malformed.";

	@Override
	protected ErrorMessage convert(MethodArgumentTypeMismatchException ex)
	{
		final ErrorMessageDetail detail = ErrorMessageDetail.builder()
				.withField(ex.getName())
				.withType(ErrorConstants.SUB_TYPE_400_INVALID_QUERY_PARAMETER)
				.build();

		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.withMoreInfo(ErrorConstants.INFO)
				.addDetails(detail).build();
	}
}
