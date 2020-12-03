package com.hybris.caas.multitenant.service.exception.converter;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.multitenant.Constants.TENANT;

/**
 * Convert a {@link MissingTenantException} to a {@link ErrorMessage}.
 */
public class MissingTenantExceptionConverter extends AbstractExceptionConverter<MissingTenantException>
{
	@Override
	protected ErrorMessage convert(MissingTenantException ex)
	{
		if (MissingTenantException.AccessType.PROTECTED.equals(ex.getAccessType()))
		{
			return ErrorMessage.builder()
					.withMessage(ex.getMessage())
					.withStatus(HttpStatus.UNAUTHORIZED.value())
					.withType(ErrorConstants.TYPE_401_INSUFFICIENT_CREDENTIALS)
					.withMoreInfo(ErrorConstants.INFO)
					.build();
		}
		else
		{
			final ErrorMessageDetail detail = ErrorMessageDetail.builder()
					.withField(TENANT)
					.withMessage(ex.getMessage())
					.withType(ErrorConstants.SUB_TYPE_400_MISSING_HEADER)
					.build();

			return ErrorMessage.builder()
					.withMessage(ErrorConstants.MESSAGE_400)
					.withStatus(HttpStatus.BAD_REQUEST.value())
					.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
					.withMoreInfo(ErrorConstants.INFO)
					.addDetails(detail)
					.build();
		}
	}
}
