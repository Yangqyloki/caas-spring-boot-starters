package com.hybris.caas.web.batch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Batch request object to encapsulate a DTO used for a service call,
 * the exception converter factory to map all exceptions to the expected
 * format, a collection of <code>ObjectNode</code> objects representing
 * the original batch items associated with this request and finally a
 * stream of <code>ExtendedBatchResponseDto</code> to keep track of the
 * final responses to provide to the API consumer.
 *
 * @param <T> The element type of the enclosed DTO.
 */
public class BatchRequest<T>
{
	private static final Logger LOG = LoggerFactory.getLogger(BatchRequest.class);

	private ExceptionConverter exceptionConverter;
	private T dto;
	private Stream<BatchResponseDto> responses;
	private Collection<ObjectNode> requests;

	public ExceptionConverter getExceptionConverter()
	{
		return exceptionConverter;
	}

	public void setExceptionConverter(final ExceptionConverter exceptionConverter)
	{
		this.exceptionConverter = exceptionConverter;
	}

	public T getDto()
	{
		return dto;
	}

	public void setDto(final T dto)
	{
		this.dto = dto;
	}

	public void setResponses(final Stream<BatchResponseDto> responses)
	{
		this.responses = responses;
	}

	public Collection<ObjectNode> getRequests()
	{
		return requests;
	}

	public void setRequests(final Collection<ObjectNode> requests)
	{
		this.requests = requests;
	}

	/**
	 * Default constructor.
	 *
	 * @param exceptionConverter this should be the <code>ExceptionConverterFactory</code>, which knows how
	 *                           to convert all exceptions to the expected format.
	 */
	public BatchRequest(ExceptionConverter exceptionConverter)
	{
		this.exceptionConverter = exceptionConverter;
	}

	/**
	 * Check if this batch request has a valid DTO.
	 *
	 * @return <code>true</code> if the enclosed DTO is not <code>null</code>; <code>false</code> otherwise
	 */
	public boolean hasDto()
	{
		return !Objects.isNull(dto);
	}

	/**
	 * Check if this batch request has a response stream.
	 *
	 * @return <code>true</code> if the enclosed response stream is not <code>null</code>; <code>false</code> otherwise
	 */
	public boolean hasResponse()
	{
		return !Objects.isNull(responses);
	}

	/**
	 * Get the current response stream.
	 *
	 * @return the current response stream or <code>Stream.empty()</code>; never <code>null</code>
	 */
	public Stream<BatchResponseDto> getResponses()
	{
		if (!hasResponse())
		{
			return Stream.empty();
		}
		return responses;
	}

	/**
	 * Returns a batch request consisting of the results of applying the given
	 * function to the DTO of this stream.
	 *
	 * <p>This is an intermediate operation.
	 *
	 * @param <R>    The element type of the new stream
	 * @param mapper a function to apply to the element
	 * @return the new batch request
	 */
	public <R> BatchRequest<R> map(Function<? super T, ? extends R> mapper)
	{
		try
		{
			return BatchRequest.of(exceptionConverter, mapper.apply(dto), requests);
		}
		catch (final RuntimeException ex)
		{
			final ErrorMessage errorMessage = exceptionConverter.toErrorMessage(ex);
			if (errorMessage.getStatus() < 500)
			{
				LOG.info("A client error occurred while mapping batch request.", ex);
			}
			else
			{
				LOG.error("An internal server error occurred while mapping batch request.", ex);
			}
			return BatchRequest.of(exceptionConverter, requests.stream()
					.map(request -> BatchResponseDto.builder()
							.status(errorMessage.getStatus())
							.body(errorMessage)
							.request(BatchResponseRequestDto.of(request))
							.build()), requests);
		}
	}

	/**
	 * Performs an action with the DTO and original requests of this batch request and returns
	 * a stream of responses.
	 *
	 * <p>This is a terminal operation.
	 *
	 * @param mapper an action to perform on the DTO and original requests which produces the final batch responses
	 */
	public Stream<BatchResponseDto> toResponseStream(BiFunction<? super T, Collection<ObjectNode>, Stream<BatchResponseDto>> mapper)
	{
		// If the response is already present, then return the response stream
		if (hasResponse())
		{
			return responses;
		}

		// Otherwise, process the batch item dto and return the resulting response stream
		try
		{
			return mapper.apply(dto, requests);
		}
		catch (final RuntimeException ex)
		{
			final ErrorMessage errorMessage = exceptionConverter.toErrorMessage(ex);
			if (errorMessage.getStatus() < 500)
			{
				LOG.info("A client error occurred while processing a batch request item.", ex);
			}
			else
			{
				LOG.error("An internal server error occurred while processing a batch request item.", ex);
			}
			return requests.stream()
					.map(request -> BatchResponseDto.builder()
							.status(errorMessage.getStatus())
							.body(errorMessage)
							.request(BatchResponseRequestDto.of(request))
							.build());
		}
	}

	/**
	 * Construct a type-safe <code>BatchRequest</code>.
	 *
	 * @param dto      the inner DTO
	 * @param requests the original requests
	 * @return the batch request object
	 */
	public static <T> BatchRequest<T> of(final ExceptionConverter exceptionConverter, final T dto,
			final Collection<ObjectNode> requests)
	{
		final BatchRequest<T> batchRequest = new BatchRequest<>(exceptionConverter);
		batchRequest.setDto(dto);
		batchRequest.setRequests(requests);
		return batchRequest;
	}

	/**
	 * Construct a type-safe <code>BatchRequest</code>.
	 *
	 * @param responses the batch item responses
	 * @param requests  the original requests
	 * @return the batch request object
	 */
	public static <R> BatchRequest<R> of(final ExceptionConverter exceptionConverter, final Stream<BatchResponseDto> responses,
			final Collection<ObjectNode> requests)
	{
		final BatchRequest<R> batchRequest = new BatchRequest<>(exceptionConverter);
		batchRequest.setResponses(responses);
		batchRequest.setRequests(requests);
		return batchRequest;
	}

}
