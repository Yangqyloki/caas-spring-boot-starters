package com.hybris.caas.error.converter.javax;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * Convert a {@link ConstraintViolationException} to a {@link ErrorMessage}.
 */
@Component
public class ConstraintViolationExceptionConverter extends AbstractExceptionConverter<ConstraintViolationException>
{
	@Override
	public ErrorMessage convert(ConstraintViolationException ex)
	{
		final ErrorMessage.Builder errorMessageBuilder = ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_400)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.withMoreInfo(ErrorConstants.INFO);

		ex.getConstraintViolations().forEach(violation -> errorMessageBuilder.addDetails(getErrorDetail(violation)));

		return errorMessageBuilder.build();
	}

	private ErrorMessageDetail getErrorDetail(final ConstraintViolation<?> violation)
	{
		return ErrorMessageDetail.builder()
				.withMessage(violation.getMessage())
				.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
				.withField(violation.getPropertyPath().toString())
				.build();
	}
}
