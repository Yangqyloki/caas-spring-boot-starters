package com.hybris.caas.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.http.HttpHeaders;

/**
 * POJO representation of a standard error schema.
 */
@JsonInclude(Include.NON_NULL)
public class ErrorMessage
{

	private Integer status;
	private String type;
	private String message;
	private String moreInfo;
	private List<ErrorMessageDetail> details = new ArrayList<>();

	@JsonIgnore
	private HttpHeaders responseHeaders = new HttpHeaders();

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
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

	public String getMoreInfo()
	{
		return moreInfo;
	}

	public void setMoreInfo(String moreInfo)
	{
		this.moreInfo = moreInfo;
	}

	public List<ErrorMessageDetail> getDetails()
	{
		return details;
	}

	public void setDetails(List<ErrorMessageDetail> details)
	{
		this.details = details;
	}

	public HttpHeaders getResponseHeaders()
	{
		return this.responseHeaders;
	}

	public void setResponseHeaders(final HttpHeaders responseHeaders)
	{
		this.responseHeaders = responseHeaders;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static final class Builder
	{
		private final ErrorMessage errorMessage;

		private Builder()
		{
			this.errorMessage = new ErrorMessage();
		}

		public Builder withStatus(int status)
		{
			this.errorMessage.setStatus(status);
			return this;
		}

		public Builder withType(String type)
		{
			this.errorMessage.setType(type);
			return this;
		}

		public Builder withMessage(String message)
		{
			this.errorMessage.setMessage(message);
			return this;
		}

		public Builder withMoreInfo(String moreInfo)
		{
			this.errorMessage.setMoreInfo(moreInfo);
			return this;
		}

		public Builder addDetails(ErrorMessageDetail... details)
		{
			this.errorMessage.details.addAll(Arrays.asList(details));
			return this;
		}

		public Builder withResponseHeaders(final HttpHeaders responseHeaders)
		{
			this.errorMessage.setResponseHeaders(responseHeaders);
			return this;
		}

		public ErrorMessage build()
		{
			return this.errorMessage;
		}

	}

	@Override
	public String toString()
	{
		return new StringBuilder("ErrorMessage [status=").append(status)
				.append(", type=").append(type)
				.append(", message=").append(message)
				.append(", moreInfo=").append(moreInfo)
				.append(", details=").append(details)
				.append(", responseHeaders=").append(responseHeaders)
				.append("]").toString();
	}

}
