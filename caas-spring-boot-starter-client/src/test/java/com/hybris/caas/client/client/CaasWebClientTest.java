package com.hybris.caas.client.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

@ExtendWith(MockitoExtension.class)
public class CaasWebClientTest
{
	private CaasWebClient caasWebClient;
	@Mock
	private WebClient webClient;
	@Mock
	private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
	@Mock
	private WebClient.ResponseSpec responseSpec;
	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	private final String tenantId = "tenantId";
	private final URI uri = URI.create("http://example.com");

	final MultiValueMap<String, String> multiValuedMap = new LinkedMultiValueMap<>();
	final ClientResponse.Builder builder = ClientResponse.create(HttpStatus.CREATED);

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void setUp()
	{
		builder.header(HttpHeaders.LOCATION, "jobs/123").body("response body");
		multiValuedMap.put(ACCEPT_LANGUAGE, Collections.singletonList("fr-CA"));
		caasWebClient = new CaasWebClient(webClient, 1);
	}

	@Test
	public void shouldSendGetRequest()
	{
		when(webClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.headers(any(Consumer.class))).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.get(tenantId, uri, String.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendGetRequestWithAdditionalHeaders()
	{
		when(webClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.headers(any(Consumer.class))).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.get(tenantId, uri, String.class, multiValuedMap);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendPostRequest()
	{
		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.post(tenantId, uri, "body", String.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendPostRequestWithAdditionalHeaders()
	{
		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.post(tenantId, uri, "body", String.class, multiValuedMap);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendPostRequestAndGetResponseWithHeadersAndStatusCode()
	{
		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.exchange()).thenReturn(Mono.just(builder.build()));
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);

		final Mono<ResponseEntity<String>> result = caasWebClient.postWithExchange(tenantId, uri, "body", String.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(ResponseEntity.class);
		assertThat(result.block().getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.block().getHeaders().getLocation().toString()).isEqualTo("jobs/123");
		assertThat(result.block().getBody()).isEqualTo("response body");
	}

	@Test
	public void shouldSendPostRequesttWithAdditionalHeadersAndGetResponseWithHeadersAndStatusCode()
	{
		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.exchange()).thenReturn(Mono.just(builder.build()));
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);

		final Mono<ResponseEntity<String>> result = caasWebClient.postWithExchange(tenantId, uri, "body", String.class,
				multiValuedMap);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(ResponseEntity.class);
		assertThat(result.block().getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.block().getHeaders().getLocation().toString()).isEqualTo("jobs/123");
		assertThat(result.block().getBody()).isEqualTo("response body");
	}

	@Test
	public void shouldSendPutRequestAndGetResponseWithHeadersAndStatusCode()
	{
		when(webClient.put()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.exchange()).thenReturn(Mono.just(builder.build()));
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);

		final Mono<ResponseEntity<String>> result = caasWebClient.putWithExchange(tenantId, uri, "body", String.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(ResponseEntity.class);
		assertThat(result.block().getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.block().getHeaders().getLocation().toString()).isEqualTo("jobs/123");
		assertThat(result.block().getBody()).isEqualTo("response body");
	}

	@Test
	public void shouldSendPutRequesttWithAdditionalHeadersAndGetResponseWithHeadersAndStatusCode()
	{
		when(webClient.put()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.exchange()).thenReturn(Mono.just(builder.build()));
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);

		final Mono<ResponseEntity<String>> result = caasWebClient.putWithExchange(tenantId, uri, "body", String.class,
				multiValuedMap);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(ResponseEntity.class);
		assertThat(result.block().getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.block().getHeaders().getLocation().toString()).isEqualTo("jobs/123");
		assertThat(result.block().getBody()).isEqualTo("response body");
	}

	@Test
	public void shouldSendPutRequest()
	{
		when(webClient.put()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.put(tenantId, uri, "body", String.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendPutRequestWithAdditionalHeaders()
	{
		when(webClient.put()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.put(tenantId, uri, "body", String.class, multiValuedMap);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendPatchRequest()
	{
		when(webClient.patch()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.patch(tenantId, uri, "body", String.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendPatchRequestWithAdditionalHeaders()
	{
		when(webClient.patch()).thenReturn(requestBodyUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(any(Consumer.class))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("result"));

		final Mono<String> result = caasWebClient.patch(tenantId, uri, "body", String.class, multiValuedMap);

		assertThat(result).isNotNull();
		assertThat(result.block()).isInstanceOf(String.class);
	}

	@Test
	public void shouldSendDeleteRequest()
	{
		when(webClient.delete()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.headers(any(Consumer.class))).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
		final Mono<Void> result = caasWebClient.delete(tenantId, uri, Void.class);

		assertThat(result).isNotNull();
		assertThat(result.block()).isNull();
	}
}