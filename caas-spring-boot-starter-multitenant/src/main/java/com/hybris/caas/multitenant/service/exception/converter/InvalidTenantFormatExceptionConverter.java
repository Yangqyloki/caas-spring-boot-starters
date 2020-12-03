package com.hybris.caas.multitenant.service.exception.converter;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.multitenant.service.exception.InvalidTenantFormatException;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.multitenant.Constants.TENANT;

/**
 * Convert a {@link InvalidTenantFormatException} to a {@link ErrorMessage}.
 */
public class InvalidTenantFormatExceptionConverter extends AbstractExceptionConverter<InvalidTenantFormatException>
{
	@Override
	protected ErrorMessage convert(InvalidTenantFormatException ex)
	{
		final ErrorMessageDetail detail = ErrorMessageDetail.builder()
				.withField(TENANT)
				.withMessage(ex.getMessage())
				.withType(ErrorConstants.SUB_TYPE_400_INVALID_HEADER)
				.build();

		return ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_400)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.addDetails(detail)
				.build();
	}
}
