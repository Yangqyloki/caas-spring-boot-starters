package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.error.exception.PayloadMalformedException;
import com.hybris.caas.web.WrappedCollection;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Spring MVC argument resolver permitting to inject a {@link WrappedCollection} of {@link BatchRequest} items
 * into a controller.
 *
 * <p> Each batch item will contain a typed DTO if the JSON was successfully converted to the DTO type
 * and if there were no validation errors. Otherwise, the batch item will contain a {@link BatchResponseDto}
 * which will hold the {@link ErrorMessage} describing the error as well as a reference to the original
 * JSON request.
 */
public class WrappedBatchRequestArgumentResolver extends BatchRequestArgumentResolver
{
	private static final String VALUE = "value";

	public WrappedBatchRequestArgumentResolver(final ObjectMapper objectMapper, final Validator validator,
			final ExceptionConverter exceptionConverter)
	{
		super(objectMapper, validator, exceptionConverter);
	}

	@Override
	public boolean supportsParameter(MethodParameter methodParameter)
	{
		return WrappedCollection.class.equals(methodParameter.getParameterType()) && supportsBatchRequests(methodParameter);
	}

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception
	{
		final List<BatchRequest<?>> batchRequests = (List<BatchRequest<?>>) super.resolveArgument(methodParameter,
				modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		return WrappedCollection.of(batchRequests);
	}

	@Override
	protected String getBatchCollectionAsString(final HttpServletRequest request) throws IOException
	{
		final String fullContent = super.getBatchCollectionAsString(request);
		final JsonNode fullObjectNode = getObjectMapper().readTree(fullContent);

		if (fullObjectNode.isObject() && !Objects.isNull(fullObjectNode.get(VALUE)) && fullObjectNode.get(VALUE).isArray())
		{
			final JsonNode batchRequests = fullObjectNode.get(VALUE);
			return getObjectMapper().writeValueAsString(batchRequests);
		}
		else
		{
			throw new PayloadMalformedException();
		}
	}

}
