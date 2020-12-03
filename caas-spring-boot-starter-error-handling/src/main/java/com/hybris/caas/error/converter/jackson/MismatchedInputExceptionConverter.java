package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Convert a {@link MismatchedInputException} to a {@link ErrorMessage}.
 */
public class MismatchedInputExceptionConverter extends AbstractJacksonExceptionConverter<MismatchedInputException>
{
	protected static final String JSON_PARSE_EXCEPTION_MESSAGE = "Data type incompatible with expected input data type.";

	@Override
	public ErrorMessage convert(MismatchedInputException ex)
	{
		final String fieldName = retrieveFieldName(ex);

		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE_BODY_INVALID)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.addDetails(ErrorMessageDetail.builder()
						.withField(fieldName)
						.withMessage(JSON_PARSE_EXCEPTION_MESSAGE)
						.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
						.withMoreInfo(buildLocationMessage(ex.getLocation()))
						.build())
				.build();
	}
}
