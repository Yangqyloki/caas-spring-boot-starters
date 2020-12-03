package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Convert a {@link JsonMappingException} to a {@link ErrorMessage}.
 */
public class JsonMappingExceptionConverter extends AbstractJacksonExceptionConverter<JsonMappingException>
{
	protected static final String JSON_PARSE_EXCEPTION_MESSAGE = "Payload parsing failed. Please ensure payload conforms to JSON specification.";

	@Override
	public ErrorMessage convert(JsonMappingException ex)
	{
		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE_BODY_INVALID)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.addDetails(ErrorMessageDetail.builder()
						.withField(retrieveFieldName(ex))
						.withMessage(buildDetailedDescription(ex))
						.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
						.withMoreInfo(buildLocationMessage(ex.getLocation()))
						.build())
				.build();
	}

}
