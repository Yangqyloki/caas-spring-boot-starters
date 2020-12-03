package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Convert a {@link InvalidTypeIdException} to a {@link ErrorMessage}.
 */
public class InvalidTypeIdExceptionConverter extends AbstractJacksonExceptionConverter<InvalidTypeIdException>
{
	@Override
	public ErrorMessage convert(InvalidTypeIdException ex)
	{
		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE_BODY_INVALID)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.build();
	}
}
