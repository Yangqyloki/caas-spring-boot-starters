package com.hybris.caas.test.web;

import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResponseUtilsTest
{
	@Test
	public void getIdFromLocationHeader_should_fail_missing_Location_header()
	{
		final Response response = new ResponseBuilder()
				.setStatusCode(200).setHeader("foo", "bar").build();
		Assertions.assertThrows(IllegalArgumentException.class, () -> ResponseUtils.getIdFromLocationHeader().apply(response));
	}

	@Test
	public void getIdFromLocationHeader_valid_path()
	{
		final Response response = new ResponseBuilder()
				.setStatusCode(200).setHeader("Location", "web://localhost:8080/test").build();
		final String location = ResponseUtils.getIdFromLocationHeader().apply(response);
		assertThat(location, equalTo("test"));
	}
}
