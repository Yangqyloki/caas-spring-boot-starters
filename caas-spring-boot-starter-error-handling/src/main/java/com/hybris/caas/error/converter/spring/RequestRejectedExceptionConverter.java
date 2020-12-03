package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;

/**
 * Convert a {@link RequestRejectedException} to a {@link ErrorMessage}.
 */
public class RequestRejectedExceptionConverter extends AbstractExceptionConverter<RequestRejectedException>
{
	protected static final String ERROR_MESSAGE_DETAILS = "The request was rejected because the URL contained a potentially malicious String. Please ensure that the URL is properly encoded.";

	@Override
	protected ErrorMessage convert(RequestRejectedException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
