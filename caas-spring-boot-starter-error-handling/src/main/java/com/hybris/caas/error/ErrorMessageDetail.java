package com.hybris.caas.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * POJO representation of a standard error detail schema.
 */
@JsonInclude(Include.NON_NULL)
public class ErrorMessageDetail<T>
{
	private String field;
	private String type;
	private String message;
	private T moreInfo;

	public String getField()
	{
		return field;
	}

	public void setField(String field)
	{
		this.field = field;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public T getMoreInfo()
	{
		return moreInfo;
	}

	public void setMoreInfo(T moreInfo)
	{
		this.moreInfo = moreInfo;
	}

	/**
	 * Creates a builder for the {@link #moreInfo} string type
	 *
	 * @return a builder for the {@link #moreInfo} string type
	 */
	public static ErrorMessageDetail.Builder<String> builder()
	{
		return new ErrorMessageDetail.Builder<>();
	}

	/**
	 * Creates a generic builder for the {@link #moreInfo} based on the generic {@code T} type
	 *
	 * @param <T> the {@link #moreInfo} type
	 * @return a generic builder for the {@link #moreInfo} based on the generic {@code T} type
	 */
	public static <T> ErrorMessageDetail.Builder<T> detailedBuilder()
	{
		return new ErrorMessageDetail.Builder<>();
	}

	public static final class Builder<T>
	{
		private final ErrorMessageDetail<T> detail;

		private Builder()
		{
			this.detail = new ErrorMessageDetail<>();
		}

		public Builder<T> withField(String field)
		{
			this.detail.setField(field);
			return this;
		}

		public Builder<T> withType(String type)
		{
			this.detail.setType(type);
			return this;
		}

		public Builder<T> withMessage(String message)
		{
			this.detail.setMessage(message);
			return this;
		}

		public Builder<T> withMoreInfo(T moreInfo)
		{
			this.detail.setMoreInfo(moreInfo);
			return this;
		}

		public ErrorMessageDetail<T> build()
		{
			return this.detail;
		}

	}

	@Override
	public String toString()
	{
		return new StringBuilder("ErrorMessageDetail [field=").append(field)
				.append(", type=").append(type)
				.append(", message=").append(message)
				.append(", moreInfo=").append(moreInfo)
				.append("]").toString();
	}
}