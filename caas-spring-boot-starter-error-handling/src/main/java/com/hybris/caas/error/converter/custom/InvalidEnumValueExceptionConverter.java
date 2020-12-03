package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.InvalidEnumValueException;
import org.springframework.http.HttpStatus;

/**
 * Converts {@link InvalidEnumValueException} to CaaS error message.
 */
public class InvalidEnumValueExceptionConverter extends AbstractExceptionConverter<InvalidEnumValueException>
{
	@Override
	public ErrorMessage convert(final InvalidEnumValueException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_400)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.withMoreInfo(ErrorConstants.INFO)
				.addDetails(ErrorMessageDetail.builder()
						.withField(ex.getFieldName())
						.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
						.withMessage(ex.getMessage())
						.withMoreInfo(ErrorConstants.INFO)
						.build())
				.build();
	}
}
