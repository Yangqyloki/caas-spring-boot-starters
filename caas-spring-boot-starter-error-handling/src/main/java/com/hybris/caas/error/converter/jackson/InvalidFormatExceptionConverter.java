package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Convert a {@link InvalidFormatException} to a {@link ErrorMessage}.
 */
public class InvalidFormatExceptionConverter extends AbstractJacksonExceptionConverter<InvalidFormatException>
{
	protected static final String JSON_MAPPING_DEFAULT_MESSAGE = "Invalid value.";
	protected static final String JSON_MAPPING_INVALID_FORMAT_DEFAULT_MESSAGE = "Invalid value. Bad input format.";
	protected static final String JSON_MAPPING_INVALID_FORMAT_ENUM_MESSAGE = "Invalid enum value. Valid values are: %s.";

	@Override
	public ErrorMessage convert(InvalidFormatException ex)
	{
		final String fieldName = retrieveFieldName(ex);

		final String errorMsg = Optional.ofNullable(ex.getTargetType())
				.map(targetType -> targetType.isEnum() ?
						String.format(JSON_MAPPING_INVALID_FORMAT_ENUM_MESSAGE, Arrays.asList(targetType.getEnumConstants())) :
						JSON_MAPPING_INVALID_FORMAT_DEFAULT_MESSAGE)
				.orElse(JSON_MAPPING_DEFAULT_MESSAGE);

		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE_BODY_INVALID)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.addDetails(ErrorMessageDetail.builder()
						.withField(fieldName)
						.withMessage(errorMsg)
						.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
						.withMoreInfo(buildLocationMessage(ex.getLocation()))
						.build())
				.build();
	}
}
