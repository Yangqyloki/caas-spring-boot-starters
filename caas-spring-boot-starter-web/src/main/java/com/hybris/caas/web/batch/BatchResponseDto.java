package com.hybris.caas.web.batch;

import com.hybris.caas.error.ErrorMessage;
import org.springframework.http.HttpHeaders;

import java.util.Objects;

public class BatchResponseDto
{
	private int status;
	private HttpHeaders headers;
	private ErrorMessage body;

	@SuppressWarnings("rawtypes")
	private BatchResponseRequestDto request;

	public int getStatus()
	{
		return status;
	}

	public void setStatus(final int status)
	{
		this.status = status;
	}

	public HttpHeaders getHeaders()
	{
		return headers;
	}

	public void setHeaders(final HttpHeaders headers)
	{
		this.headers = headers;
	}

	public ErrorMessage getBody()
	{
		return body;
	}

	public void setBody(final ErrorMessage body)
	{
		this.body = body;
	}

	public BatchResponseRequestDto getRequest()
	{
		return request;
	}

	public void setRequest(final BatchResponseRequestDto request)
	{
		this.request = request;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		final BatchResponseDto that = (BatchResponseDto) o;
		return status == that.status && Objects.equals(headers, that.headers) && Objects.equals(body, that.body) && Objects.equals(
				request, that.request);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(status, headers, body, request);
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private BatchResponseDto batchResponseDto;

		public Builder()
		{
			batchResponseDto = new BatchResponseDto();
		}

		public Builder status(final int status)
		{
			batchResponseDto.setStatus(status);
			return this;
		}

		public Builder headers(final HttpHeaders headers)
		{
			batchResponseDto.setHeaders(headers);
			return this;
		}

		public Builder body(final ErrorMessage body)
		{
			batchResponseDto.setBody(body);
			return this;
		}

		public Builder request(final BatchResponseRequestDto request)
		{
			batchResponseDto.setRequest(request);
			return this;
		}

		public BatchResponseDto build()
		{
			return batchResponseDto;
		}

	}
}
