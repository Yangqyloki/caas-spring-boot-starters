package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Convert a {@link JsonParseException} to a {@link ErrorMessage}.
 */
public class JsonParseExceptionConverter extends AbstractExceptionConverter<JsonParseException>
{
	protected static final String JSON_PARSE_EXCEPTION_MESSAGE = "Payload parsing failed. Please ensure payload conforms to JSON specification. Error at [line: %s, column: %s].";

	@Override
	public ErrorMessage convert(JsonParseException ex)
	{
		final JsonLocation location = ex.getLocation();

		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE_BODY_INVALID)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.addDetails(ErrorMessageDetail.builder()
						.withMessage(String.format(JSON_PARSE_EXCEPTION_MESSAGE, location.getLineNr(), location.getColumnNr()))
						.build())
				.build();

	}
}
