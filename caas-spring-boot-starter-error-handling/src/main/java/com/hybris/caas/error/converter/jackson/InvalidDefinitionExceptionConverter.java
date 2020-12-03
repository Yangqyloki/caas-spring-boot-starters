package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

/**
 * Convert a {@link InvalidDefinitionException} to a {@link ErrorMessage}.
 */
public class InvalidDefinitionExceptionConverter extends AbstractJacksonExceptionConverter<InvalidDefinitionException>
{
	protected static final String INVALID_DEFINITION_EXCEPTION_MESSAGE = "Invalid field definition '%s'.";
	protected static final String JSON_MAPPING_INVALID_FORMAT_ENUM_MESSAGE = "Invalid enum value. Valid values are: %s.";

	@Override
	public ErrorMessage convert(InvalidDefinitionException ex)
	{
		final String fieldName = retrieveFieldName(ex);
		final String propertyName = Optional.ofNullable(ex.getProperty()).map(BeanPropertyDefinition::getName).orElse("unknown");
		final String errorMsg = Optional.ofNullable(ex.getType())
				.map(javaType -> javaType.isEnumType() ?
						String.format(JSON_MAPPING_INVALID_FORMAT_ENUM_MESSAGE,
								Arrays.asList(javaType.getRawClass().getEnumConstants())) :
						String.format(INVALID_DEFINITION_EXCEPTION_MESSAGE, propertyName))
				.orElse(String.format(INVALID_DEFINITION_EXCEPTION_MESSAGE, propertyName));

		return ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_400)
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
