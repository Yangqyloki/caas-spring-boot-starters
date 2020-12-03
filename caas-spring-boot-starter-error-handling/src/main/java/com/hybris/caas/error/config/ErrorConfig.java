package com.hybris.caas.error.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hybris.caas.error.aspect.IgnoreExceptionAspect;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.converter.DefaultExceptionConverter;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.error.converter.ExceptionConverterFactory;
import com.hybris.caas.error.converter.concurrent.CompletionExceptionConverter;
import com.hybris.caas.error.converter.concurrent.ExecutionExceptionConverter;
import com.hybris.caas.error.converter.custom.InvalidEnumValueExceptionConverter;
import com.hybris.caas.error.converter.custom.InvalidHttpRequestHeaderExceptionConverter;
import com.hybris.caas.error.converter.custom.MissingHttpRequestHeaderExceptionConverter;
import com.hybris.caas.error.converter.custom.PathSegmentConstraintViolationExceptionConverter;
import com.hybris.caas.error.converter.custom.TooManyRequestsExceptionConverter;
import com.hybris.caas.error.converter.custom.UpdateConflictExceptionConverter;
import com.hybris.caas.error.converter.jackson.InvalidDefinitionExceptionConverter;
import com.hybris.caas.error.converter.jackson.InvalidFormatExceptionConverter;
import com.hybris.caas.error.converter.jackson.InvalidTypeIdExceptionConverter;
import com.hybris.caas.error.converter.jackson.JsonMappingExceptionConverter;
import com.hybris.caas.error.converter.jackson.JsonParseExceptionConverter;
import com.hybris.caas.error.converter.jackson.MismatchedInputExceptionConverter;
import com.hybris.caas.error.converter.jackson.UnrecognizedPropertyExceptionConverter;
import com.hybris.caas.error.converter.javax.ConstraintViolationExceptionConverter;
import com.hybris.caas.error.converter.javax.UndeclaredThrowableExceptionConverter;
import com.hybris.caas.error.converter.javax.ValidationExceptionConverter;
import com.hybris.caas.error.converter.spring.BindExceptionConverter;
import com.hybris.caas.error.converter.spring.HttpMediaTypeNotAcceptableExceptionConverter;
import com.hybris.caas.error.converter.spring.HttpMediaTypeNotSupportedExceptionConverter;
import com.hybris.caas.error.converter.spring.HttpMessageConversionExceptionConverter;
import com.hybris.caas.error.converter.spring.HttpMessageNotReadableExceptionConverter;
import com.hybris.caas.error.converter.spring.HttpRequestMethodNotSupportedExceptionConverter;
import com.hybris.caas.error.converter.spring.MaxUploadSizeExceededExceptionConverter;
import com.hybris.caas.error.converter.spring.MethodArgumentNotValidExceptionConverter;
import com.hybris.caas.error.converter.spring.MethodArgumentTypeMismatchExceptionConverter;
import com.hybris.caas.error.converter.spring.MissingRequestHeaderExceptionConverter;
import com.hybris.caas.error.converter.spring.MissingServletRequestParameterExceptionConverter;
import com.hybris.caas.error.converter.spring.MultipartExceptionConverter;
import com.hybris.caas.error.converter.spring.NestedServletExceptionConverter;
import com.hybris.caas.error.converter.spring.NoHandlerFoundExceptionConverter;
import com.hybris.caas.error.converter.spring.RequestRejectedExceptionConverter;
import com.hybris.caas.error.exception.InvalidEnumValueException;
import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;
import com.hybris.caas.error.exception.MissingHttpRequestHeaderException;
import com.hybris.caas.error.exception.PathSegmentConstraintViolationException;
import com.hybris.caas.error.exception.TooManyRequestsException;
import com.hybris.caas.error.exception.UpdateConflictException;
import com.hybris.caas.error.web.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.NestedServletException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@ConditionalOnWebApplication
@Configuration("errorConfig")
@AutoConfigureBefore({ ErrorMvcAutoConfiguration.class })
public class ErrorConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(ErrorConfig.class);

	@Bean
	public AbstractExceptionConverter<Exception> defaultExceptionConverter()
	{
		return new DefaultExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<BindException> bindExceptionConverter()
	{
		return new BindExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MethodArgumentNotValidException> methodArgumentNotValidExceptionConverter()
	{
		return new MethodArgumentNotValidExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<HttpMediaTypeNotSupportedException> httpMediaTypeNotSupportedExceptionConverter()
	{
		return new HttpMediaTypeNotSupportedExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<HttpMediaTypeNotAcceptableException> httpMediaTypeNotAcceptableExceptionConverter()
	{
		return new HttpMediaTypeNotAcceptableExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<HttpRequestMethodNotSupportedException> httpRequestMethodNotSupportedExceptionConverter()
	{
		return new HttpRequestMethodNotSupportedExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<HttpMessageNotReadableException> httpMessageNotReadableExceptionConverter()
	{
		return new HttpMessageNotReadableExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<HttpMessageConversionException> httpMessageConversionExceptionConverter()
	{
		return new HttpMessageConversionExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InvalidHttpRequestHeaderException> invalidHttpRequestHeaderExceptionConverter()
	{
		return new InvalidHttpRequestHeaderExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MissingHttpRequestHeaderException> missingHttpRequestHeaderExceptionConverter()
	{
		return new MissingHttpRequestHeaderExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<UpdateConflictException> updateConflictException()
	{
		return new UpdateConflictExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<NoHandlerFoundException> noHandlerFoundExceptionConverter()
	{
		return new NoHandlerFoundExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<RequestRejectedException> requestRejectedExceptionConverter()
	{
		return new RequestRejectedExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<ConstraintViolationException> constraintViolationExceptionConverter()
	{
		return new ConstraintViolationExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InvalidFormatException> invalidFormatExceptionConverter()
	{
		return new InvalidFormatExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InvalidTypeIdException> invalidTypeIdExceptionConverter()
	{
		return new InvalidTypeIdExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InvalidDefinitionException> invalidDefinitionExceptionConverter()
	{
		return new InvalidDefinitionExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<UnrecognizedPropertyException> unrecognizedPropertyExceptionConverter()
	{
		return new UnrecognizedPropertyExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MismatchedInputException> mismatchedInputExceptionConverter()
	{
		return new MismatchedInputExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<JsonParseException> jsonParseExceptionConverter()
	{
		return new JsonParseExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<JsonMappingException> jsonMappingExceptionConverter()
	{
		return new JsonMappingExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<PathSegmentConstraintViolationException> pathSegmentConstraintViolationExceptionConverter()
	{
		return new PathSegmentConstraintViolationExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MethodArgumentTypeMismatchException> methodArgumentTypeMismatchExceptionConverter()
	{
		return new MethodArgumentTypeMismatchExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MissingRequestHeaderException> missingRequestHeaderExceptionConverter()
	{
		return new MissingRequestHeaderExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MissingServletRequestParameterException> missingServletRequestParameterExceptionConverter()
	{
		return new MissingServletRequestParameterExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<ValidationException> validationExceptionConverter()
	{
		return new ValidationExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<CompletionException> completionExceptionConverter()
	{
		return new CompletionExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<ExecutionException> executionExceptionConverter()
	{
		return new ExecutionExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<MaxUploadSizeExceededException> maxUploadSizeExceededExceptionConverter(
			final MultipartProperties properties)
	{
		return new MaxUploadSizeExceededExceptionConverter(properties.getMaxFileSize());
	}

	@Bean
	public AbstractExceptionConverter<MultipartException> multipartExceptionConverter()
	{
		return new MultipartExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<TooManyRequestsException> tooManyRequestExceptionConverter()
	{
		return new TooManyRequestsExceptionConverter();
	}
		
	@Bean
	@ConditionalOnMissingBean(name = "invalidEnumValueExceptionConverter")
	public AbstractExceptionConverter<InvalidEnumValueException> invalidEnumValueExceptionConverter()
	{
		return new InvalidEnumValueExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<UndeclaredThrowableException> undeclaredThrowableExceptionConverter()
	{
		return new UndeclaredThrowableExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<NestedServletException> nestedServletExceptionConverter()
	{
		return new NestedServletExceptionConverter();
	}

	@Bean
	public ExceptionConverter exceptionConverterFactory()
	{
		return new ExceptionConverterFactory(defaultExceptionConverter());
	}

	@Bean
	public GlobalExceptionHandler globalExceptionHandler(@Qualifier("exceptionConverterFactory") final ExceptionConverter exceptionConverter)
	{
		return new GlobalExceptionHandler(exceptionConverter);
	}

	@Bean
	public FilterRegistrationBean globalExceptionHandlerFilterRegistration(final GlobalExceptionHandler globalExceptionHandler)
	{
		LOG.info("CaaS Error Handling - registered global exception handling filter at order " + Ordered.HIGHEST_PRECEDENCE);

		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(globalExceptionHandler);
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registrationBean;
	}

	@Bean
	public IgnoreExceptionAspect ignoreExceptionAspect()
	{
		return new IgnoreExceptionAspect();
	}

}
