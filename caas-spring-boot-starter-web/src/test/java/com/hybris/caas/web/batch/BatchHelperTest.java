package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.hybris.caas.web.batch.BatchHelper.buildBatchResponses;
import static com.hybris.caas.web.batch.BatchHelper.buildMultiStatusResponse;
import static com.hybris.caas.web.batch.BatchHelper.processBatch;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.MULTI_STATUS;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RunWith(MockitoJUnitRunner.class)
public class BatchHelperTest
{
	private static final String EN_US = "en-US";
	@Mock
	private ExceptionConverter exceptionConverter;
	@Mock
	private ArrayList<ObjectNode> dummyArray;
	private final ObjectNode validNode = JsonNodeFactory.instance.objectNode();

	@Before
	public void setup()
	{
		validNode.put("id", "1");
		when(exceptionConverter.toErrorMessage(Mockito.any(RuntimeException.class))).thenReturn(
				ErrorMessage.builder().withStatus(400).build());
	}

	@Test
	public void should_process_batch_response()
	{
		final BatchRequest<String> request = BatchRequest.of(exceptionConverter, "test", Collections.singleton(validNode));
		final ResponseEntity<List<BatchResponseDto>> response = processBatch(asList(request), (dto) -> dummyArray.size(),
				HttpStatus.NO_CONTENT);

		assertThat(response.getStatusCode(), is(MULTI_STATUS));
		final BatchResponseDto batchResponseDto = response.getBody().stream().findFirst().get();
		assertThat(batchResponseDto.getStatus(), is(NO_CONTENT.value()));
		assertThat(batchResponseDto.getRequest().getBody(), is(validNode));
		verify(dummyArray, times(1)).size();
	}

	@Test
	public void should_process_batch_with_error()
	{

		when(dummyArray.size()).thenThrow(new RuntimeException());

		final BatchRequest<String> request = BatchRequest.of(exceptionConverter, "test", Collections.singleton(validNode));
		final ResponseEntity<List<BatchResponseDto>> response = processBatch(asList(request), (dto) -> dummyArray.size(),
				HttpStatus.NO_CONTENT);

		assertThat(response.getStatusCode(), is(MULTI_STATUS));
		final BatchResponseDto batchResponseDto = response.getBody().stream().findFirst().get();
		assertThat(batchResponseDto.getStatus(), is(BAD_REQUEST.value()));
		assertThat(batchResponseDto.getRequest().getBody(), is(validNode));
	}

	@Test
	public void should_build_no_content_batch_response()
	{
		final BatchResponseDto response = buildBatchResponses(asList(validNode), HttpStatus.NO_CONTENT).findFirst().get();
		assertThat(response.getStatus(), is(NO_CONTENT.value()));
		assertThat(response.getRequest().getBody(), is(validNode));
	}

	@Test
	public void should_build_created_batch_response()
	{
		final Function<ObjectNode, HttpHeaders> headersFunction = req -> new HttpHeaders();
		final BatchResponseDto response = buildBatchResponses(asList(validNode), headersFunction, HttpStatus.CREATED).findFirst()
				.get();
		assertThat(response.getStatus(), is(CREATED.value()));
		assertThat(response.getRequest().getBody(), is(validNode));
		assertThat(response.getHeaders(), notNullValue());
	}

	@Test
	public void should_build_multi_status()
	{
		final BatchRequest<String> request = BatchRequest.of(exceptionConverter, "test", Collections.singleton(validNode));
		final BiFunction<String, Collection<ObjectNode>, Stream<BatchResponseDto>> processor = (a, b) -> Stream.of(
				BatchResponseDto.builder().build());

		final ResponseEntity<List<BatchResponseDto>> response = buildMultiStatusResponse(asList(request), processor, new HttpHeaders());

		assertThat(response.getStatusCode(), is(MULTI_STATUS));
		assertThat(response.getBody().size(), is(1));
		assertThat(response.getHeaders().size(), is(0));
	}

	@Test
	public void should_build_multi_status_with_response_header()
	{
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_LANGUAGE, EN_US);
		final BatchRequest<String> request = BatchRequest.of(exceptionConverter, "test", Collections.singleton(validNode));
		final BiFunction<String, Collection<ObjectNode>, Stream<BatchResponseDto>> processor = (a, b) -> Stream.of(
				BatchResponseDto.builder().build());

		final ResponseEntity<List<BatchResponseDto>> response = buildMultiStatusResponse(asList(request), processor, httpHeaders);

		assertThat(response.getStatusCode(), is(MULTI_STATUS));
		assertThat(response.getBody().size(), is(1));
		assertThat(response.getHeaders().get(HttpHeaders.CONTENT_LANGUAGE).get(0), is(EN_US));
	}

	@Test
	public void should_build_multi_status_process_batch()
	{
		final BatchRequest<String> request = BatchRequest.of(exceptionConverter, "test", Collections.singleton(validNode));
		final Function<String, Integer> processor = k -> 0;
		final BiFunction<String, Integer, HttpStatus> httpStatus = (dto, value) -> HttpStatus.OK;
		final BiFunction<String, Integer, HttpHeaders> httpHeaders = (dto, value) -> null;

		final ResponseEntity<List<BatchResponseDto>> response = processBatch(singletonList(request), processor, httpStatus,
				httpHeaders, new HttpHeaders());

		assertThat(response.getStatusCode(), is(MULTI_STATUS));
		assertThat(response.getBody().size(), is(1));
		assertThat(response.getHeaders().size(), is(0));
	}

	@Test
	public void should_build_multi_status_process_batch_with_response_header()
	{
		final HttpHeaders globalHttpHeaders = new HttpHeaders();
		globalHttpHeaders.add(HttpHeaders.CONTENT_LANGUAGE, EN_US);
		final BatchRequest<String> request = BatchRequest.of(exceptionConverter, "test", Collections.singleton(validNode));
		final Function<String, Integer> processor = k -> 0;
		final BiFunction<String, Integer, HttpStatus> httpStatus = (dto, value) -> HttpStatus.OK;
		final BiFunction<String, Integer, HttpHeaders> httpHeaders = (dto, value) -> null;

		final ResponseEntity<List<BatchResponseDto>> response = processBatch(singletonList(request), processor, httpStatus,
				httpHeaders, globalHttpHeaders);

		assertThat(response.getStatusCode(), is(MULTI_STATUS));
		assertThat(response.getBody().size(), is(1));
		assertThat(response.getHeaders().get(HttpHeaders.CONTENT_LANGUAGE).get(0), is(EN_US));
	}

}
