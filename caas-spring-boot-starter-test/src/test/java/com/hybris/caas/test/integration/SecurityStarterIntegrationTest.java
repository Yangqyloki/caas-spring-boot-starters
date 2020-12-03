package com.hybris.caas.test.integration;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.test.security.CaasJwtToken;
import com.hybris.caas.test.security.TokenFactory;
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
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class SecurityStarterIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private TokenFactory tokenFactory;

	private HttpHeaders headers;
	private HttpEntity<?> request;
	private String authenticatedToken;
	private String authorizedToken;

	@BeforeEach
	public void setUp()
	{
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		request = new HttpEntity<>(headers);

		authenticatedToken = CaasJwtToken.DEFAULT_TOKEN;
		authorizedToken = tokenFactory.getToken("test-user");
	}

	@Test
	public void should_allow_public_request_without_token()
	{
		final ResponseEntity<String> response = restTemplate.exchange("/security/public", GET, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		assertThat(response.getBody()).isEqualTo("OK");
	}

	@Test
	public void should_convert_401_unauthorized()
	{
		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/security/authenticated", GET, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(response.getBody().getStatus()).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().getType()).isEqualTo(ErrorConstants.TYPE_401_INSUFFICIENT_CREDENTIALS);
	}

	@Test
	public void should_allow_request_with_valid_authentication_token()
	{
		headers.setBearerAuth(authenticatedToken);
		request = new HttpEntity<>(headers);

		final ResponseEntity<String> response = restTemplate.exchange("/security/authenticated", GET, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		assertThat(response.getBody()).isEqualTo("OK");
	}

	@Test
	public void should_convert_403_forbidden()
	{
		// Default token does not have any scopes
		headers.setBearerAuth(authenticatedToken);
		request = new HttpEntity<>(headers);

		final ResponseEntity<ErrorMessage> response = restTemplate.exchange("/security/authorized", GET, request, ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(response.getBody().getStatus()).isEqualTo(FORBIDDEN.value());
		assertThat(response.getBody().getType()).isEqualTo(ErrorConstants.TYPE_403_INSUFFICIENT_PERMISSIONS);
	}

	@Test
	public void should_allow_request_with_valid_authorization_token_scope()
	{
		headers.setBearerAuth(authorizedToken);
		request = new HttpEntity<>(headers);

		final ResponseEntity<String> response = restTemplate.exchange("/security/authorized", GET, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		assertThat(response.getBody()).isEqualTo("OK");
	}

}
