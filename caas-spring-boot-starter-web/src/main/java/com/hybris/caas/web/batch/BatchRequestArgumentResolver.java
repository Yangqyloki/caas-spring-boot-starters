package com.hybris.caas.web.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.error.exception.PayloadMalformedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Spring MVC argument resolver permitting to inject a collection of {@link BatchRequest} items
 * into a controller.
 *
 * <p> Each batch item will contain a typed DTO if the JSON was successfully converted to the DTO type
 * and if there were no validation errors. Otherwise, the batch item will contain a {@link BatchResponseDto}
 * which will hold the {@link ErrorMessage} describing the error as well as a reference to the original
 * JSON request.
 */
public class BatchRequestArgumentResolver implements HandlerMethodArgumentResolver
{
	private static final String TYPE_REGEX = "^.*BatchRequest<(.*)>>$";
	private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_REGEX);

	private final ObjectMapper objectMapper;
	private final Validator validator;
	private final ExceptionConverter exceptionConverter;

	public BatchRequestArgumentResolver(final ObjectMapper objectMapper, final Validator validator, @Qualifier("exceptionConverterFactory") final ExceptionConverter exceptionConverter)
	{
		this.objectMapper = objectMapper;
		this.validator = validator;
		this.exceptionConverter = exceptionConverter;
	}

	@Override
	public boolean supportsParameter(MethodParameter methodParameter)
	{
		return Collection.class.isAssignableFrom(methodParameter.getParameterType()) && supportsBatchRequests(methodParameter);

	}

	protected boolean supportsBatchRequests(MethodParameter methodParameter)
	{
		return TYPE_PATTERN.matcher(methodParameter.getGenericParameterType().getTypeName()).matches();
	}

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception
	{
		final HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

		// Get batch request DTO type from method parameter generic parameter type
		final Matcher matcher = TYPE_PATTERN.matcher(methodParameter.getGenericParameterType().getTypeName());
		if (!matcher.find())
		{
			throw new IllegalArgumentException("Exception getting type name from regex pattern.");
		}
		final String typeName = matcher.group(1);

		// Convert request input stream to object node array, then to a collection of validated dtos, then to a collection of batch requests.
		try
		{
			final String content = getBatchCollectionAsString(request);
	        final ObjectNode[] payload = objectMapper.readValue(content, ObjectNode[].class);

	        // Check for null payload
	        if (Objects.isNull(payload))
	        {
	        		throw new PayloadMalformedException();
	        }

	        // Check for null in list
	        final List<ObjectNode> payloadList = Arrays.asList(payload);
	        if (payloadList.contains(null))
	        {
	        		throw new PayloadMalformedException();
	        }

	        return prepareBatch(payloadList, Class.forName(typeName), getValidationGroups(methodParameter));
		}
		catch (final JsonProcessingException ex)
		{
			throw new PayloadMalformedException(ex);
		}
	}

	/**
	 * Extract the batch request collection from the request.
	 *
	 * @param request the http request
	 * @return the json array string representing the batch
	 * @throws IOException when an error occurs extracting the request body
	 */
	protected String getBatchCollectionAsString(final HttpServletRequest request) throws IOException
	{
		return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
	}

	/**
	 * Get the validation groups from the {@link Validated} annotation.
	 *
	 * @param methodParameter the method parameter
	 * @return the validation groups provided in the <code>Validated</code> annotation or an empty array if none were found
	 */
	protected Class<?>[] getValidationGroups(final MethodParameter methodParameter)
	{
		return Optional.ofNullable(methodParameter.getParameterAnnotation(Validated.class))
				.map(annotation -> annotation.value())
				.orElse(new Class<?>[] {});
	}

	/**
	 * Prepare a batch by converting a list of raw JSON to a stream of valid DTOs.
	 *
	 * @param payload list of raw JSON objects
	 * @param dtoClass DTO type
	 * @param validationGroups the validation groups to apply during validation
	 * @return stream of batch request DTOs encapsulating DTO and matching original requests
	 */
	protected <T> Collection<BatchRequest<T>> prepareBatch(final List<ObjectNode> payload, final Class<T> dtoClass, final Class<?>[] validationGroups)
	{
		return payload.stream()
		// Map<ObjectNode, List<ObjectNode>> - (Object Node, List of Duplicates)
		.collect(Collectors.groupingBy(dto -> dto, LinkedHashMap::new, Collectors.toList()))
		.entrySet().stream()
		// Stream<BatchRequest<ObjectNode>> - (ObjectNode, List of Duplicates)
		.map(entry -> BatchRequest.of(exceptionConverter, entry.getKey(), entry.getValue()))
		// Convert ObjectNode to DTO
		.map(batchRequest -> batchRequest.map((ObjectNode node) -> convertToDto(node, dtoClass)))
		// Validate DTO if it is not null
		.map(batchRequest -> batchRequest.hasDto() ? batchRequest.map(dto -> validateDto(dto, validationGroups)) : batchRequest)
		// Stream<BatchRequest<T>> - (DTO, Response, Number of Occurrences)
		.collect(Collectors.toList());
	}

	/**
	 * Convert a <code>ObjectNode</code> to a DTO.
	 *
	 * @param node the object node
	 * @param clazz the type to convert to
	 * @return the DTO of type T
	 * @throws PayloadMalformedException when a {@link JsonProcessingException} occurs
	 */
	protected <T> T convertToDto(final ObjectNode node, final Class<T> clazz)
	{
		try
		{
			return objectMapper.treeToValue(node, clazz);
		}
		catch (final JsonProcessingException e)
		{
			throw new PayloadMalformedException(e);
		}
	}

	/**
	 * Validate a given DTO using an injected validator.
	 *
	 * @param dto the dto to validate
	 * @param validationGroups the validation groups to apply during validation
	 * @return the original dto
	 * @throws ConstraintViolationException if validation fails
	 */
	protected <T> T validateDto(final T dto, final Class<?>[] validationGroups)
	{
		final Set<ConstraintViolation<T>> constraintViolations = validator.validate(dto, validationGroups);
		if (!constraintViolations.isEmpty())
		{
			throw new ConstraintViolationException(constraintViolations);
		}
		return dto;
	}

	public ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

	public Validator getValidator()
	{
		return validator;
	}

	public ExceptionConverter getExceptionConverter()
	{
		return exceptionConverter;
	}
}
