package com.hybris.caas.swagger;

import java.util.ArrayList;
import java.util.List;

public class ApiDescriptionsDto
{
	private List<ApiDescriptionDto> apiDescriptions = new ArrayList<>();

	public ApiDescriptionsDto(final List<ApiDescriptionDto> apiDescriptions)
	{
		this.apiDescriptions = apiDescriptions;
	}

	public List<ApiDescriptionDto> getApiDescriptions()
	{
		return apiDescriptions;
	}

	public void setApiDescriptions(final List<ApiDescriptionDto> apiDescriptions)
	{
		this.apiDescriptions = apiDescriptions;
	}

	public static ApiDescriptionsDto of(final List<ApiDescriptionDto> apiDescriptions)
	{
		return new ApiDescriptionsDto(apiDescriptions);
	}

}
