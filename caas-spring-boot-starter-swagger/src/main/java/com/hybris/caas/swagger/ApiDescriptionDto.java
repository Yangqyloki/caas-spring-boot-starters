package com.hybris.caas.swagger;

import javax.validation.constraints.NotEmpty;

public class ApiDescriptionDto
{
	@NotEmpty
	private String name;
	@NotEmpty
	private String url;

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(final String url)
	{
		this.url = url;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private ApiDescriptionDto apiDescriptionDto;

		public Builder()
		{
			apiDescriptionDto = new ApiDescriptionDto();
		}

		public Builder name(final String name)
		{
			apiDescriptionDto.setName(name);
			return this;
		}

		public Builder url(final String url)
		{
			apiDescriptionDto.setUrl(url);
			return this;
		}
		public ApiDescriptionDto build()
		{
			return apiDescriptionDto;
		}
	}

}
