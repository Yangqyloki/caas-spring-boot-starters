package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.error.exception.PayloadMalformedException;
import com.hybris.caas.web.WrappedCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BatchRequestArgumentResolverTest
{
	private static final String TYPE_NAME = "java.util.List<com.hybris.caas.web.batch.BatchRequest<com.hybris.caas.web.batch.BatchRequestArgumentResolverTest$SampleDto>>";

	private BatchRequestArgumentResolver batchRequestArgumentResolver;
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ErrorMessage errorMessage = ErrorMessage.builder().withStatus(500).build();
	private Class parameterType = Collection.class;

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
	@Mock
	private Validated validated;

	@Before
	public void setup() throws IOException
	{
		batchRequestArgumentResolver = new BatchRequestArgumentResolver(objectMapper, validator, exceptionConverter);

		when(exceptionConverter.toErrorMessage(Mockito.any())).thenReturn(errorMessage);
		when(methodParameter.getParameterType()).thenReturn(parameterType);
		when(methodParameter.getGenericParameterType()).thenReturn(genericParameterType);
		when(methodParameter.getParameterAnnotation(Validated.class)).thenReturn(validated);
		when(validated.value()).thenReturn(new Class<?>[] {SampleDto.SourceGroup.class, SampleDto.TargetGroup.class});
		when(genericParameterType.getTypeName()).thenReturn(TYPE_NAME);
		when(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

		final String content = "[{\"source\":\"AAA\", \"target\":\"BBB\"}]";
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
		final Collection<BatchRequest<SampleDto>> result = (Collection<BatchRequest<SampleDto>>) batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

		final BatchRequest<SampleDto> request = result.iterator().next();
		assertThat(request.getDto().getSource(), equalTo("AAA"));
		assertThat(request.getDto().getTarget(), equalTo("BBB"));

		final ObjectNode node = request.getRequests().iterator().next();
		assertThat(node.get("source").asText(), equalTo("AAA"));
		assertThat(node.get("target").asText(), equalTo("BBB"));
		assertThat(request.getResponses().count(), equalTo(0L));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_resolve_argument_preserving_request_order() throws Exception
	{
		final String content = "[{\"source\":\"AAA\", \"target\":\"BBB\"}, {\"source\":\"CCC\", \"target\":\"DDD\"}, {\"source\":\"EEE\", \"target\":\"FFF\"}]";
		when(httpServletRequest.getInputStream()).thenReturn(
				new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));
		final Collection<BatchRequest<SampleDto>> result = (Collection<BatchRequest<SampleDto>>) batchRequestArgumentResolver.resolveArgument(
				methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

		final List<SampleDto> dtoList = StreamSupport.stream(Spliterators.spliteratorUnknownSize(result.iterator(), 0), false)
				.map(BatchRequest::getDto)
				.collect(Collectors.toList());

		assertThat(dtoList, contains(
				SampleDto.of("AAA", "BBB"),
				SampleDto.of("CCC", "DDD"),
				SampleDto.of("EEE", "FFF")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_resolve_argument_with_duplicates() throws Exception
	{
		final String content = "[{\"source\":\"AAA\", \"target\":\"BBB\"}, {\"source\":\"AAA\", \"target\":\"BBB\"}]";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));
		final Collection<BatchRequest<SampleDto>> result = (Collection<BatchRequest<SampleDto>>) batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

		final BatchRequest<SampleDto> request = result.iterator().next();
		assertThat(request.getDto().getSource(), equalTo("AAA"));
		assertThat(request.getDto().getTarget(), equalTo("BBB"));

		ObjectNode node = request.getRequests().iterator().next();
		assertThat(node.get("source").asText(), equalTo("AAA"));
		assertThat(node.get("target").asText(), equalTo("BBB"));

		node = request.getRequests().iterator().next();
		assertThat(node.get("source").asText(), equalTo("AAA"));
		assertThat(node.get("target").asText(), equalTo("BBB"));
		assertThat(request.getResponses().count(), equalTo(0L));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_resolve_argument_with_conversion_error() throws Exception
	{
		final String content = "[{\"foo\":\"A\", \"bar\":\"B\"}]";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		final Collection<BatchRequest<SampleDto>> result = (Collection<BatchRequest<SampleDto>>) batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

		final BatchRequest<SampleDto> request = result.iterator().next();
		assertThat(request.getDto(), nullValue());

		final BatchResponseDto response = request.getResponses().findFirst().get();
		assertThat(response.getBody(), equalTo(errorMessage));
		assertThat(response.getStatus(), equalTo(500));

		final ObjectNode node = request.getRequests().iterator().next();
		assertThat(node.get("foo").asText(), equalTo("A"));
		assertThat(node.get("bar").asText(), equalTo("B"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_resolve_argument_with_validation_error() throws Exception
	{
		final String content = "[{\"source\":\"A\", \"target\":\"B\"}]";
		when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(content.getBytes())));

		final Collection<BatchRequest<SampleDto>> result = (Collection<BatchRequest<SampleDto>>) batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

		final BatchRequest<SampleDto> request = result.iterator().next();
		assertThat(request.getDto(), nullValue());

		final BatchResponseDto response = request.getResponses().findFirst().get();
		assertThat(response.getBody(), equalTo(errorMessage));
		assertThat(response.getStatus(), equalTo(500));

		final ObjectNode node = request.getRequests().iterator().next();
		assertThat(node.get("source").asText(), equalTo("A"));
		assertThat(node.get("target").asText(), equalTo("B"));
	}

	@Test
	public void should_not_support_parameter_no_generics()
	{
		when(genericParameterType.getTypeName()).thenReturn("com.hybris.caas.web.batch.BatchRequest<com.hybris.caas.web.batch.BatchRequestArgumentResolverTest$SampleDto>");
		final boolean supported = batchRequestArgumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.FALSE));
	}

	@Test
	public void should_not_support_parameter_not_a_collection()
	{
		parameterType = WrappedCollection.class;
		when(methodParameter.getParameterType()).thenReturn(parameterType);

		final boolean supported = batchRequestArgumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.FALSE));
	}

	@Test
	public void should_not_support_parameter_no_BatchRequest()
	{
		when(genericParameterType.getTypeName()).thenReturn("java.util.List<com.hybris.caas.web.batch.BatchResponse<com.hybris.caas.web.batch.BatchRequestArgumentResolverTest$SampleDto>>");
		final boolean supported = batchRequestArgumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.FALSE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_resolve_argument_regex_no_match() throws Exception
	{
		when(genericParameterType.getTypeName()).thenReturn("INVALID");
		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		fail();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IOException.class)
	public void should_fail_resolve_argument_regex_inputstream_error() throws Exception
	{
		when(httpServletRequest.getInputStream()).thenThrow(IOException.class);
		batchRequestArgumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		fail();
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_resolve_argument_regex_json_conversion_error() throws Exception
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

	@Test
	public void should_validate_dto_no_groups_no_errors()
	{
		final SampleDto dto = SampleDto.of("111", "222");
		final SampleDto response = batchRequestArgumentResolver.validateDto(dto, new Class<?>[] {});

		assertThat(response.getSource(), equalTo("111"));
		assertThat(response.getTarget(), equalTo("222"));
	}

	@Test
	public void should_validate_dto_no_groups_with_errors()
	{
		final SampleDto dto = SampleDto.of("12", "12");
		final SampleDto response = batchRequestArgumentResolver.validateDto(dto, new Class<?>[] {});

		assertThat(response.getSource(), equalTo("12"));
		assertThat(response.getTarget(), equalTo("12"));
	}

	@Test
	public void should_validate_dto_with_groups()
	{
		final SampleDto dto = SampleDto.of("111", "222");
		final SampleDto response = batchRequestArgumentResolver.validateDto(dto, new Class<?>[] {SampleDto.SourceGroup.class, SampleDto.TargetGroup.class});

		assertThat(response.getSource(), equalTo("111"));
		assertThat(response.getTarget(), equalTo("222"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void should_fail_validate_dto_with_groups()
	{
		final SampleDto dto = SampleDto.of("12", "12");
		batchRequestArgumentResolver.validateDto(dto, new Class<?>[] {SampleDto.SourceGroup.class, SampleDto.TargetGroup.class});
		fail();
	}

	@Test(expected = ConstraintViolationException.class)
	public void should_fail_validate_dto_with_source_group()
	{
		final SampleDto dto = SampleDto.of("12", "12");
		batchRequestArgumentResolver.validateDto(dto, new Class<?>[] {SampleDto.SourceGroup.class});
		fail();
	}

	@Test(expected = ConstraintViolationException.class)
	public void should_fail_validate_dto_with_target_group()
	{
		final SampleDto dto = SampleDto.of("12", "12");
		batchRequestArgumentResolver.validateDto(dto, new Class<?>[] {SampleDto.TargetGroup.class});
		fail();
	}

	@Test
	public void should_convert_dto()
	{
		final ObjectNode node = buildValidNode();
		final SampleDto response = batchRequestArgumentResolver.convertToDto(node, SampleDto.class);

		assertThat(response.getSource(), equalTo("A"));
		assertThat(response.getTarget(), equalTo("B"));
	}

	@Test(expected = PayloadMalformedException.class)
	public void should_fail_convert_dto()
	{
		final ObjectNode node = buildInvalidNode();
		batchRequestArgumentResolver.convertToDto(node, SampleDto.class);
		fail();
	}

	private ObjectNode buildValidNode()
	{
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("source", "A");
		node.put("target", "B");
		return node;
	}

	private ObjectNode buildInvalidNode()
	{
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("invalid", "invalid");
		node.put("foo", "bar");
		return node;
	}

	private static class SampleDto
	{
		@Size(min = 3, groups = SourceGroup.class)
		private String source;
		@Size(min = 3, groups = TargetGroup.class)
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

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			final SampleDto sampleDto = (SampleDto) o;
			return Objects.equals(source, sampleDto.source) && Objects.equals(target, sampleDto.target);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(source, target);
		}

		public static SampleDto of(final String source, final String target)
		{
			final SampleDto sampleDto = new SampleDto();
			sampleDto.setSource(source);
			sampleDto.setTarget(target);
			return sampleDto;
		}

		interface SourceGroup {}
		interface TargetGroup {}
	}
}
