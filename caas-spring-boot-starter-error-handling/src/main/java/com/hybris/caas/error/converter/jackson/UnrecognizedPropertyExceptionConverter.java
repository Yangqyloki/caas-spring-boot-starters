package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Convert a {@link UnrecognizedPropertyException} to a {@link ErrorMessage}.
 */
public class UnrecognizedPropertyExceptionConverter extends AbstractExceptionConverter<UnrecognizedPropertyException>
{
	protected static final String UNRECOGNIZED_PROPERTY_EXCEPTION_MESSAGE = "Field '%s' is not recognized.";

	@Override
	public ErrorMessage convert(UnrecognizedPropertyException ex)
	{
		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE_BODY_INVALID)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.addDetails(ErrorMessageDetail.builder()
						.withField(ex.getPropertyName())
						.withMessage(String.format(UNRECOGNIZED_PROPERTY_EXCEPTION_MESSAGE, ex.getPropertyName()))
						.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
						.build())
				.build();
	}
}

