package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper for processing batch request/response.
 */
public final class BatchHelper
{
	private BatchHelper()
	{
		//empty
	}

	/**
	 * Build the MULTI_STATUS response for the processed batch requests.
	 *
	 * @param batchRequests the mapped batch requests
	 * @param operation     the {@link Consumer} operation to be executed before building the response
	 * @param status        the {@link HttpStatus} to return for each successfully processed request
	 * @param <K>           the DTO type to map the response. It's also passed as argument of {@link Consumer#accept(Object)}
	 * @return the batch item responses
	 */
	public static <K> ResponseEntity<List<BatchResponseDto>> processBatch(final List<BatchRequest<K>> batchRequests,
			final Consumer<K> operation, final HttpStatus status)
	{
		return process(batchRequests, operation, requests -> buildBatchResponses(requests, status));
	}

	/**
	 * Build the MULTI_STATUS response for the processed batch requests.
	 *
	 * @param batchRequests   the mapped batch requests
	 * @param operation       the {@link Consumer} operation to be executed before building the response
	 * @param status          the {@link HttpStatus} to return for each successfully processed request
	 * @param headersFunction the function to apply to produce the {@link HttpHeaders} for each original request
	 * @param <K>             the DTO type to map the response. It's also passed as argument of {@link Consumer#accept(Object)}
	 * @return the batch item responses
	 */
	public static <K> ResponseEntity<List<BatchResponseDto>> processBatch(final List<BatchRequest<K>> batchRequests,
			final Consumer<K> operation, final HttpStatus status, final Function<ObjectNode, HttpHeaders> headersFunction)
	{
		return process(batchRequests, operation, requests -> buildBatchResponses(requests, headersFunction, status));
	}

	/**
	 * Build the MULTI_STATUS response for the processed batch requests.
	 *
	 * @param batchRequests     the mapped batch requests
	 * @param processor         the {@link Function} operation to be executed before building the response
	 * @param httpStatus        the {@link HttpStatus} function to return for each successfully processed request
	 * @param httpHeaders       the {@link HttpHeaders} function to produce for each successfully processed request
	 * @param globalHttpHeaders the {@link HttpHeaders} as part of the overall response
	 * @param <K>               the DTO type to map the response. Passed as argument to the {@code httpStatus} and {@code httpHeaders} functions
	 * @param <V>               the return value type produced by the {@code processor}. Passed as argument to the {@code httpStatus} and {@code httpHeaders} functions
	 * @return a multi status response composed of batch responses
	 */
	public static <K, V> ResponseEntity<List<BatchResponseDto>> processBatch(final List<BatchRequest<K>> batchRequests,
			final Function<K, V> processor, final BiFunction<K, V, HttpStatus> httpStatus,
			final BiFunction<K, V, HttpHeaders> httpHeaders, final HttpHeaders globalHttpHeaders)
	{
		final BiFunction<K, Collection<ObjectNode>, Stream<BatchResponseDto>> multiprocessor = (dto, requests) ->
		{

			final V value = processor.apply(dto);

			return requests.stream()
					.map(req -> BatchResponseDto.builder()
							.status(httpStatus.apply(dto, value).value())
							.headers(httpHeaders.apply(dto, value))
							.request(BatchResponseRequestDto.of(req))
							.build());
		};

		return buildMultiStatusResponse(batchRequests, multiprocessor, globalHttpHeaders);
	}

	/**
	 * Build the MULTI_STATUS response for the processed batch requests.
	 *
	 * @param batchRequests     the mapped batch requests
	 * @param processor         the function to be used for processing batch requests
	 * @param <K>               the DTO type to map the response. It's also passed as argument of {@link Consumer#accept(Object)}
	 * @param globalHttpHeaders the {@link HttpHeaders} as part of the overall response
	 * @return the MULTI_STATUS response
	 */
	public static <K> ResponseEntity<List<BatchResponseDto>> buildMultiStatusResponse(final List<BatchRequest<K>> batchRequests,
			final BiFunction<K, Collection<ObjectNode>, Stream<BatchResponseDto>> processor, final HttpHeaders globalHttpHeaders)
	{
		final List<BatchResponseDto> responses = batchRequests.stream()
				.flatMap(batchRequest -> batchRequest.toResponseStream(processor))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.MULTI_STATUS).headers(globalHttpHeaders).body(responses);
	}

	/**
	 * Build a batch response for each given request.
	 *
	 * @param requests    the original requests that map to batch item
	 * @param httpHeaders the function to apply to produce the {@link HttpHeaders} for each original request
	 * @param status      the {@link HttpStatus} to return with each successfully processed request
	 * @return the batch item responses
	 */
	public static Stream<BatchResponseDto> buildBatchResponses(final Collection<ObjectNode> requests,
			final Function<ObjectNode, HttpHeaders> httpHeaders, final HttpStatus status)
	{
		return requests.stream()
				.map(req -> BatchResponseDto.builder()
						.status(status.value())
						.headers(httpHeaders.apply(req))
						.request(BatchResponseRequestDto.of(req))
						.build());
	}

	/**
	 * Build a batch response for each given request.
	 *
	 * @param requests the original requests that map to batch item
	 * @param status   the {@link HttpStatus} to return with each successfully processed request
	 * @return the batch item responses
	 */
	public static Stream<BatchResponseDto> buildBatchResponses(final Collection<ObjectNode> requests, final HttpStatus status)
	{
		return requests.stream()
				.map(req -> BatchResponseDto.builder().status(status.value()).request(BatchResponseRequestDto.of(req)).build());
	}

	private static <K> ResponseEntity<List<BatchResponseDto>> process(final List<BatchRequest<K>> batchRequests,
			final Consumer<K> consumer, final Function<Collection<ObjectNode>, Stream<BatchResponseDto>> functionResponse)
	{
		final BiFunction<K, Collection<ObjectNode>, Stream<BatchResponseDto>> processor = (dto, requests) ->
		{
			consumer.accept(dto);
			return functionResponse.apply(requests);
		};
		return buildMultiStatusResponse(batchRequests, processor, new HttpHeaders());
	}
}
