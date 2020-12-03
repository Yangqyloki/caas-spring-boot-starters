package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BatchRequestTest
{
	private static final String TYPE = "type";
	private static final String TEST_ERROR = "test-error";
	private static final int _500 = 500;

	private BatchRequest<String> batchRequest;
	private final BatchResponseDto successResponse = BatchResponseDto.builder().status(200).build();
	private final ErrorMessage errorMessage = ErrorMessage.builder().withStatus(_500).withMessage(TEST_ERROR).withType(TYPE).build();

	@Mock
	private ExceptionConverter exceptionConverter;

	@Before
	public void setUp()
	{
		batchRequest = new BatchRequest<>(exceptionConverter);
		when(exceptionConverter.toErrorMessage(any())).thenReturn(errorMessage);
	}

	@Test
	public void should_have_dto()
	{
		batchRequest.setDto("string");
		assertThat(batchRequest.hasDto(), equalTo(Boolean.TRUE));
	}

	@Test
	public void should_not_have_dto()
	{
		assertThat(batchRequest.hasDto(), equalTo(Boolean.FALSE));
	}

	@Test
	public void should_have_response_empty_stream()
	{
		batchRequest.setResponses(Stream.empty());
		assertThat(batchRequest.hasResponse(), equalTo(Boolean.TRUE));
	}

	@Test
	public void should_have_response_nonempty_stream()
	{
		batchRequest.setResponses(Stream.of(new BatchResponseDto()));
		assertThat(batchRequest.hasResponse(), equalTo(Boolean.TRUE));
	}

	@Test
	public void should_not_have_response()
	{
		assertThat(batchRequest.hasResponse(), equalTo(Boolean.FALSE));
	}

	@Test
	public void should_map_to_batch_request()
	{
		// Given
		final Collection<ObjectNode> nodes = Collections.singleton(buildObjectNode());
		batchRequest.setDto("string");
		batchRequest.setResponses(Stream.of(new BatchResponseDto()));
		batchRequest.setRequests(nodes);

		// When
		final BatchRequest<Integer> mappedRequest = batchRequest.map(string -> Integer.valueOf(5));

		// Then
		assertThat(mappedRequest.getDto(), equalTo(5));
		assertThat(mappedRequest.getResponses().count(), equalTo(0L));
		assertThat(mappedRequest.getRequests(), equalTo(nodes));
		assertThat(mappedRequest.getExceptionConverter(), equalTo(exceptionConverter));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_fail_map_to_batch_request()
	{
		// Given
		final Function<String, Integer> mapper = mock(Function.class);
		doThrow(IllegalArgumentException.class).when(mapper).apply(anyString());

		final ObjectNode node = buildObjectNode();
		final Collection<ObjectNode> nodes = Arrays.asList(node, node);
		batchRequest.setDto("string");
		batchRequest.setResponses(Stream.of(new BatchResponseDto()));
		batchRequest.setRequests(nodes);

		// When
		final BatchRequest<Integer> mappedRequest = batchRequest.map(mapper);

		// Then
		assertThat(mappedRequest.getDto(), nullValue());
		assertThat(mappedRequest.getRequests(), equalTo(nodes));
		assertThat(mappedRequest.getExceptionConverter(), equalTo(exceptionConverter));

		final List<BatchResponseDto> responses = new ArrayList<>();
		mappedRequest.getResponses().forEach(response -> {
			assertThat(response.getBody(), equalTo(errorMessage));
			assertThat(response.getStatus(), equalTo(_500));
			assertThat(response.getHeaders(), nullValue());
			assertThat(response.getRequest().getBody(), equalTo(node));
			responses.add(response);
		});
		assertThat(responses, hasSize(2));
	}

	@Test
	public void should_map_to_existing_responses()
	{
		// Given
		final BatchResponseDto response = new BatchResponseDto();
		batchRequest.setResponses(Stream.of(new BatchResponseDto()));

		// When
		final Stream<BatchResponseDto> responses = batchRequest.toResponseStream((string, requests) -> Stream.of(successResponse));

		// Then
		final List<BatchResponseDto> responseList = responses.collect(Collectors.toList());
		assertThat(responseList, hasSize(1));
		assertThat(responseList.get(0), equalTo(response));
	}

	@Test
	public void should_map_to_responses()
	{
		// Given
		final Collection<ObjectNode> nodes = Collections.singleton(buildObjectNode());
		batchRequest.setDto("string");
		batchRequest.setRequests(nodes);

		// When
		final Stream<BatchResponseDto> responses = batchRequest.toResponseStream((string, requests) -> Stream.of(successResponse));

		// Then
		final List<BatchResponseDto> responseList = responses.collect(Collectors.toList());
		assertThat(responseList, hasSize(1));
		assertThat(responseList.get(0), equalTo(successResponse));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_fail_map_to_responses()
	{
		// Given
		final BiFunction<String, Collection<ObjectNode>, Stream<BatchResponseDto>> mapper = mock(BiFunction.class);
		doThrow(IllegalArgumentException.class).when(mapper).apply(anyString(), any(Collection.class));

		final ObjectNode node = buildObjectNode();
		final Collection<ObjectNode> nodes = Arrays.asList(node, node);
		batchRequest.setDto("string");
		batchRequest.setRequests(nodes);

		// When
		final Stream<BatchResponseDto> responses = batchRequest.toResponseStream(mapper);

		// Then
		final List<BatchResponseDto> responseList = new ArrayList<>();
		responses.forEach(response -> {
			assertThat(response.getBody(), equalTo(errorMessage));
			assertThat(response.getStatus(), equalTo(_500));
			assertThat(response.getHeaders(), nullValue());
			assertThat(response.getRequest().getBody(), equalTo(node));
			responseList.add(response);
		});
		assertThat(responseList, hasSize(2));
	}

	private ObjectNode buildObjectNode()
	{
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("foo", "bar");
		return node;
	}
}
