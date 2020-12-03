package com.hybris.caas.test.integration;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.test.integration.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

public class ErrorStarterIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private TestRestTemplate restTemplate;
	private HttpHeaders headers;
	private HttpEntity<?> request;

	@BeforeEach
	public void setUp()
	{
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		request = new HttpEntity<>(headers);
	}

	@Test
	public void should_convert_400_validation_violation()
	{
		request = new HttpEntity<>(Person.NO_NAME, headers);
		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/persons", POST, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		final ErrorMessage errorMessage = response.getBody();
		assertThat(errorMessage.getStatus()).isEqualTo(BAD_REQUEST.value());
		assertThat(errorMessage.getType()).isEqualTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION);

		final ErrorMessageDetail errorMessageDetail = errorMessage.getDetails().get(0);
		assertThat(errorMessageDetail.getType()).isEqualTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD);
		assertThat(errorMessageDetail.getField()).isEqualTo("name");
		assertThat(errorMessageDetail.getMessage()).isEqualTo("must not be empty");
	}

	@Test
	public void should_convert_405_method_not_allowed()
	{
		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/dummy", PUT, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
		assertThat(response.getBody().getStatus()).isEqualTo(METHOD_NOT_ALLOWED.value());
		assertThat(response.getBody().getType()).isEqualTo(ErrorConstants.TYPE_405_UNSUPPORTED_METHOD);
	}

	@Test
	public void should_convert_415_unsupported_media_type()
	{
		headers.setContentType(MediaType.APPLICATION_XML);
		final HttpEntity<String> request = new HttpEntity<>(headers);

		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/dummy", GET, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
		assertThat(response.getHeaders().getAccept()).containsOnly(MediaType.APPLICATION_JSON);
		assertThat(response.getBody().getStatus()).isEqualTo(UNSUPPORTED_MEDIA_TYPE.value());
		assertThat(response.getBody().getType()).isEqualTo(ErrorConstants.TYPE_415_UNSUPPORTED_REQUEST_CONTENT_TYPE);
	}

	@Test
	public void should_convert_exception_from_filter_before_calling_chain()
	{
		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/fail?location=before", GET, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
		assertThat(response.getBody().getType()).isEqualTo(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void should_convert_exception_from_filter_after_calling_chain()
	{
		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/fail?location=after", GET, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
		assertThat(response.getBody().getType()).isEqualTo(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR);
	}

}
