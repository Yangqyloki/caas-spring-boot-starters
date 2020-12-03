package com.hybris.caas.error.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.ClassUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends OncePerRequestFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final ExceptionConverter exceptionConverter;
	private final ObjectMapper objectMapper;
	private final MethodParameter methodParameter;
	private final ContentNegotiationManager contentNegotiationManager;
	private final List<HttpMessageConverter<?>> messageConverters;
	private HttpEntityMethodProcessor httpEntityMethodProcessor;

	public GlobalExceptionHandler(final ExceptionConverter exceptionConverter)
	{
		this.exceptionConverter = exceptionConverter;
		this.objectMapper = new ObjectMapper();

		// MethodParameter is required for the HttpEntityMethodProcessor.
		// This MethodParameter should represent a generic ResponseEntity, where the generic parameter type can be extracted at runtime.
		final Method method = ClassUtils.getMethod(GlobalExceptionHandler.class, "wrapErrorMessageInResponseEntity", ErrorMessage.class);
		this.methodParameter = new MethodParameter(method, -1);
		this.methodParameter.getGenericParameterType();

		this.contentNegotiationManager = new ContentNegotiationManager(
				new HeaderContentNegotiationStrategy(), new FixedContentNegotiationStrategy(MediaType.APPLICATION_JSON));
		this.messageConverters = new ArrayList<>();
		this.messageConverters.add(new MappingJackson2HttpMessageConverter());
		this.httpEntityMethodProcessor = new HttpEntityMethodProcessor(messageConverters, contentNegotiationManager);
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<String> handleHttpMediaTypeNotAcceptableException(final HttpMediaTypeNotAcceptableException exception)
	{
		final ErrorMessage errorMessage = convertAndLog(exception);
		return new ResponseEntity<>(errorMessage.getResponseHeaders(), HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler({ AccessDeniedException.class })
	public ResponseEntity<ErrorMessage> handleAccessDeniedException(final AccessDeniedException exception) throws AccessDeniedException
	{
		throw exception;
	}

	@ExceptionHandler({ Throwable.class })
	public ResponseEntity<ErrorMessage> handleThrowable(final Throwable exception)
	{
		final ErrorMessage errorMessage = convertAndLog(exception);
		return wrapErrorMessageInResponseEntity(errorMessage);
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain)
	{
		try
		{
			filterChain.doFilter(request, response);
		}
		catch (final Throwable e)
		{
			final ErrorMessage errorMessage = convertAndLog(e);
			final ResponseEntity<ErrorMessage> responseEntity = wrapErrorMessageInResponseEntity(errorMessage);
			try
			{
				final MethodParameter localMethodParameter = new MethodParameter(methodParameter);
				final ModelAndViewContainer mavContainer = new ModelAndViewContainer();

				httpEntityMethodProcessor.handleReturnValue(responseEntity, localMethodParameter, mavContainer,
						new ServletWebRequest(request, response));
			}
			catch (final Exception ex)
			{
				LOG.error("Failed to process error response: {}", errorMessage, ex);
			}
		}
	}

	/**
	 * Convert exception to {@link ErrorMessage} and log the exception.
	 * >= 5xx errors are logged to ERROR
	 * < 5xx errors are logged to INFO
	 *
	 * @param exception the error that was caught
	 * @return the error message as a response entity
	 */
	protected ErrorMessage convertAndLog(final Throwable exception)
	{
		final ErrorMessage errorMessage = exceptionConverter.toErrorMessage(exception);
		if (errorMessage.getStatus() < 500)
		{
			log4xxError(exception, errorMessage);
		}
		else
		{
			log5xxError(exception, errorMessage);
		}
		return errorMessage;
	}

	/**
	 * Wrap the {@link ErrorMessage} POJO into a Spring {@link ResponseEntity}.
	 *
	 * NOTE: Method must be public to be discovered via reflection.
	 *
	 * @param errorMessage the error message POJO
	 * @return the response entity
	 */
	public ResponseEntity<ErrorMessage> wrapErrorMessageInResponseEntity(final ErrorMessage errorMessage)
	{
		return new ResponseEntity<>(errorMessage, errorMessage.getResponseHeaders(), HttpStatus.valueOf(errorMessage.getStatus()));
	}

	protected void log4xxError(final Throwable exception, final ErrorMessage errorMessage)
	{

		LOG.info("\n*** 4xx exception occurred: \n" + getPrettyPrintedErrorMessage(errorMessage) + "\n", exception);
	}

	protected void log5xxError(final Throwable exception, final ErrorMessage errorMessage)
	{
		LOG.error("\n*** 5xx exception occurred: \n" + getPrettyPrintedErrorMessage(errorMessage) + "\n", exception);
	}

	protected String getPrettyPrintedErrorMessage(final ErrorMessage errorMessage)
	{
		try
		{
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMessage);
		}
		catch (final JsonProcessingException ex)
		{
			LOG.warn("An unexpected error occurred while trying to print an error message.", ex);
			return "";
		}
	}

	public HttpEntityMethodProcessor getHttpEntityMethodProcessor()
	{
		return httpEntityMethodProcessor;
	}

	public void setHttpEntityMethodProcessor(final HttpEntityMethodProcessor httpEntityMethodProcessor)
	{
		this.httpEntityMethodProcessor = httpEntityMethodProcessor;
	}
}
