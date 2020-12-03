package com.hybris.caas.web.interceptor;

import com.hybris.caas.web.exception.UriLengthValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Validates the length of the URI request and throws {@link UriLengthValidation} exception if URI is longer than a configurable length.
 */
@Component
public class UriLengthValidationInterceptor extends HandlerInterceptorAdapter
{
	public static final int DEFAULT_MAX_URI_LENGTH = 1024 * 4;

	private final int maxUriLength;

	public UriLengthValidationInterceptor(@Value("${MAX_URI_LENGTH:0}") int maxUriLength)
	{
		this.maxUriLength = maxUriLength > 0 ? maxUriLength : DEFAULT_MAX_URI_LENGTH;
	}

	public int getMaxUriLength()
	{
		return maxUriLength;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		if ((handler instanceof HandlerMethod) && ((HandlerMethod) handler).getMethod().isAnnotationPresent(UriLengthValidation.class))
		{
			final int annotationMaxLengthValue = ((HandlerMethod) handler).getMethod()
					.getAnnotation(UriLengthValidation.class)
					.maxLength();
			final String queryString = request.getQueryString();

			// +1 to count the ? separator if query string is available
			final int uriLength =
					request.getRequestURL().length() + Optional.ofNullable(queryString).map(qs -> queryString.length() + 1).orElse(0);

			if (uriLength > getApplicableMaxLength(annotationMaxLengthValue))
			{
				throw new UriLengthValidationException(getApplicableMaxLength(annotationMaxLengthValue), uriLength);
			}
		}

		return super.preHandle(request, response, handler);
	}

	/**
	 * Retrieve the maximum URI length to apply. It will be validated in this order:
	 * 1- If the annotation contains the maxLength attribute, this value will be the maximum limit.
	 * 2- If the environment variable is defined, it will be the maximum limit.
	 * 3- The default value will be the maximum limit.
	 *
	 * @param annotationMaxLengthValue The maximum length provided by annotation.
	 * @Retruns the maximum length of the URI.
	 */
	private int getApplicableMaxLength(final int annotationMaxLengthValue)
	{
		final boolean isProvidedByAnnotation = annotationMaxLengthValue > 0 && annotationMaxLengthValue != DEFAULT_MAX_URI_LENGTH;
		return isProvidedByAnnotation ? annotationMaxLengthValue : this.maxUriLength;
	}
}
