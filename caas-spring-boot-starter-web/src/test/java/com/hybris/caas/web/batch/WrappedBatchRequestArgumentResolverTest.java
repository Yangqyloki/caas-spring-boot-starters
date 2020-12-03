package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.error.exception.PayloadMalformedException;
import com.hybris.caas.web.WrappedCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WrappedBatchRequestArgumentResolverTest
{
	private static final String TYPE_NAME = "com.hybris.caas.web.WrappedCollection<com.hybris.caas.web.batch.BatchRequest<com.hybris.caas.web.batch.WrappedBatchRequestArgumentResolverTest$SampleDto>>";
	private WrappedBatchRequestArgumentResolver batchRequestArgumentResolver;
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private Class parameterType = WrappedCollection.class;

	@Mock
	private ExceptionConverter exceptionConverter;
	@Mock
	private MethodParameter methodParameter;
	@Mock
	private Type genericParameterType;
	@Mock
	private ModelAndViewContainer modelAndViewContainer;
	@Mock
	private NativeWebRequest nativeWebRequest;
	@Mock
	private HttpServletRequest httpServletRequest;
	@Mock
	private WebDataBinderFactory webDataBinderFactory;

	@Before
	public void setup() throws IOException
	{
		batchRequestArgumentResolver = new WrappedBatchRequestArgumentResolver(objectMapper, validator, exceptionConverter);

		when(methodParameter.getParameterType()).thenReturn(parameterType);
		when(methodParameter.getGenericParameterType()).thenReturn(genericParameterType);
		when(genericParameterType.getTypeName()).thenReturn(TYPE_NAME);
		when(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

		final String content = "{\"value\":[{\"source\":\"AAA\", \"target\":\"BBB\"}]}";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));
	}

	@Test
	public void should_support_parameter()
	{
		final boolean supported = batchRequestArgumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.TRUE));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_resolve_argument_with_dto() throws Exception
	{
		final WrappedCollection<BatchRequest<SampleDto>> result = (WrappedCollection<BatchRequest<SampleDto>>) batchRequestArgumentResolver
				.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

		final Collection<BatchRequest<SampleDto>> batchResult = result.getValue();
		final BatchRequest<SampleDto> request = batchResult.iterator().next();
		assertThat(request.getDto().getSource(), equalTo("AAA"));
		assertThat(request.getDto().getTarget(), equalTo("BBB"));

		final ObjectNode node = request.getRequests().iterator().next();
		assertThat(node.get("source").asText(), equalTo("AAA"));
		assertThat(node.get("target").asText(), equalTo("BBB"));
		assertThat(request.getResponses().count(), equalTo(0L));
	}

	@Test
	public void should_not_support_parameter_not_WrappedCollection()
	{
		parameterType = Collection.class;
		when(methodParameter.getParameterType()).thenReturn(parameterType);

		final boolean supported = batchRequestArgumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.FALSE));
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IOException.class)
	public void should_fail_resolve_argument_inputstream_error() throws Exception
	{
		when(httpServletRequest.getInputStream()).thenThrow(IOException.class);
		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		fail();
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_argument_json_conversion_error() throws Exception
	{
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("INVALID".getBytes())));
		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		fail();
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_when_payload_is_null() throws Exception
	{
		final String content = "null";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_when_payload_has_null_items() throws Exception
	{
		final String content = "[null,null]";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_when_payload_is_top_level_array() throws Exception
	{
		final String content = "[{\"source\":\"AAA\", \"target\":\"BBB\"}]";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_when_payload_is_incorrect() throws Exception
	{
		final String content = "{[{\"source\":\"AAA\", \"target\":\"BBB\"}]}";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_when_payload_is_missing_value_property() throws Exception
	{
		final String content = "{\"source\":\"AAA\", \"target\":\"BBB\"}";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_when_payload_is_value_property_is_not_array() throws Exception
	{
		final String content = "{\"value\":\"AAA\"}";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
	}

	private static class SampleDto
	{
		private String source;
		private String target;

		public String getSource()
		{
			return source;
		}

		public void setSource(final String source)
		{
			this.source = source;
		}

		public String getTarget()
		{
			return target;
		}

		public void setTarget(final String target)
		{
			this.target = target;
		}

	}
}
