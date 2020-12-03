package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.PathSegmentConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;

/**
 * Convert a {@link PathSegmentConstraintViolationException} to a {@link ErrorMessage}.
 */
@Component
public class PathSegmentConstraintViolationExceptionConverter
		extends AbstractExceptionConverter<PathSegmentConstraintViolationException>
{
	@Override
	public ErrorMessage convert(final PathSegmentConstraintViolationException ex)
	{
		final ErrorMessage.Builder errorMessageBuilder = ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_404)
				.withStatus(HttpStatus.NOT_FOUND.value())
				.withType(ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
				.withMoreInfo(ErrorConstants.INFO);

		ex.getConstraintViolations().forEach(violation -> errorMessageBuilder.addDetails(getErrorDetail(violation)));

		return errorMessageBuilder.build();
	}

	private ErrorMessageDetail getErrorDetail(final ConstraintViolation<?> violation)
	{
		return ErrorMessageDetail.builder()
				.withMessage(violation.getMessage())
				.withType(ErrorConstants.SUB_TYPE_400_INVALID_PATH_SEGMENT)
				.withField(violation.getPropertyPath().toString())
				.build();
	}
}
