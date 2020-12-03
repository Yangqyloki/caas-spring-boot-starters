package com.hybris.caas.web.batch;

import java.util.Objects;

public class BatchResponseRequestDto<T>
{
	private T body;

	public T getBody()
	{
		return body;
	}

	public void setBody(final T body)
	{
		this.body = body;
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
		final BatchResponseRequestDto<?> that = (BatchResponseRequestDto<?>) o;
		return Objects.equals(body, that.body);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(body);
	}

	public static <T> BatchResponseRequestDto<T> of(T body)
	{
		final BatchResponseRequestDto<T> batchResponseRequestDto = new BatchResponseRequestDto<>();
		batchResponseRequestDto.setBody(body);
		return batchResponseRequestDto;
	}
}
