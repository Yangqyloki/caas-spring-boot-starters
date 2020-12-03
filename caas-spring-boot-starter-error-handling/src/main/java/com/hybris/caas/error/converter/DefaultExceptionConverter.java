package com.hybris.caas.error.converter;

import org.springframework.http.HttpStatus;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.annotation.WebException;

/**
 * Default implementation of the {@link ExceptionConverter}. The error message's
 * "message" attribute will always be populated with
 * {@link Exception#getMessage()}.
 * <p>
 * If the exception being converted is annotated with a {@link WebException}
 * annotation, then the annotation parameters will be used to populate the error
 * message.
 * </p>
 * If the exception being converted is not annotated with a
 * {@link WebException} annotation, then the following default values are used:
 * <ul>
 * <li>status: <code>500</code></li>
 * <li>message: </li>
 * <li>type: <code>internal_service_error</code></li>
 * <li>moreInfo: </li>
 * </ul>
 */
public class DefaultExceptionConverter extends AbstractExceptionConverter<Exception>
{

	@Override
	public ErrorMessage convert(final Exception ex)
	{
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		String type = ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR;
		String info = ErrorConstants.INFO;
		String message = ErrorConstants.MESSAGE_500;

		final WebException annotation = ex.getClass().getAnnotation(WebException.class);
		if (annotation != null)
		{
			status = annotation.status();
			type = annotation.type();
			info = annotation.info();
			message = ex.getMessage();
		}

		return ErrorMessage.builder().withStatus(status.value())
				.withType(type)
				.withMoreInfo(info)
				.withMessage(message)
				.build();
	}
}
