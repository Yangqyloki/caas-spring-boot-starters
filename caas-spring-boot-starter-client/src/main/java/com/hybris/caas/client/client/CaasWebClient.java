package com.hybris.caas.client.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Base client that for making REST calls using {@link WebClient}.
 */
public class CaasWebClient
{
	private static final Logger LOG = LoggerFactory.getLogger(CaasWebClient.class);

	private final WebClient webClient;
	private final int retriesMaxAttempts;

	public CaasWebClient(final WebClient webClient, int retriesMaxAttempts)
	{
		this.webClient = webClient;
		this.retriesMaxAttempts = retriesMaxAttempts;
	}

	/**
	 * Gets data from a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param responseClass the {@link Class} of the response body type
	 * @param <T>           the response body type
	 * @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> get(final String authorization, final URI uri, final Class<T> responseClass)
	{
		return webClient.get()
				.uri(uri)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while getting data from {}: {}", uri.getPath(), e.toString()))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Gets data from a specified {@link URI}.
	 *
	 * @param authorization     the authorization value
	 * @param uri               the {@link URI} of the endpoint to make the request to
	 * @param responseClass     the {@link Class} of the response body type
	 * @param additionalHeaders a List of additional headers.
	 * @param <T>               the response body type
	 * @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> get(final String authorization, final URI uri, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return webClient.get()
				.uri(uri)
				.headers(h -> {
					if (!CollectionUtils.isEmpty(additionalHeaders))
					{
						h.addAll(additionalHeaders);
					}
					h.add(HttpHeaders.AUTHORIZATION, authorization);
				})
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while getting data from {}: {}", uri.getPath(), e.toString()))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Posts data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @param responseClass the {@link Class} of the response body type
	 * @param <T>           the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> post(final String authorization, final URI uri, final Object body, final Class<T> responseClass)
	{
		return webClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Posts data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @param typeReference a type reference describing the expected response body type
	 * @param <T>           the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> post(final String authorization, final URI uri, final Object body,
			final ParameterizedTypeReference<T> typeReference)
	{
		return webClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(typeReference)
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Posts data to a specified {@link URI}.
	 *
	 * @param authorization     the authorization value
	 * @param uri               the {@link URI} of the endpoint to make the request to
	 * @param body              the body of the request
	 * @param responseClass     the {@link Class} of the response body type
	 * @param additionalHeaders a List of additional headers.
	 * @param <T>               the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> post(final String authorization, final URI uri, final Object body, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return webClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> {
					if (!CollectionUtils.isEmpty(additionalHeaders))
					{
						h.addAll(additionalHeaders);
					}
					h.add(HttpHeaders.AUTHORIZATION, authorization);
				})
				.bodyValue(body)
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Put data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @param responseClass the {@link Class} of the response body type
	 * @param <T>           the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> put(final String authorization, final URI uri, final Object body, final Class<T> responseClass)
	{
		return webClient.put()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while putting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Put data to a specified {@link URI}.
	 *
	 * @param authorization     the authorization value
	 * @param uri               the {@link URI} of the endpoint to make the request to
	 * @param body              the body of the request
	 * @param responseClass     the {@link Class} of the response body type
	 * @param additionalHeaders a List of additional headers.
	 * @param <T>               the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> put(final String authorization, final URI uri, final Object body, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return webClient.put()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> {
					if (!CollectionUtils.isEmpty(additionalHeaders))
					{
						h.addAll(additionalHeaders);
					}
					h.add(HttpHeaders.AUTHORIZATION, authorization);
				})
				.bodyValue(body)
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while putting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Patch data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @param responseClass the {@link Class} of the response body type
	 * @param <T>           the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> patch(final String authorization, final URI uri, final Object body, final Class<T> responseClass)
	{
		return webClient.patch()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while patching data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Patch data to a specified {@link URI}.
	 *
	 * @param authorization     the authorization value
	 * @param uri               the {@link URI} of the endpoint to make the request to
	 * @param body              the body of the request
	 * @param responseClass     the {@link Class} of the response body type
	 * @param additionalHeaders a List of additional headers.
	 * @param <T>               the response body type
	 * @return @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> patch(final String authorization, final URI uri, final Object body, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return webClient.patch()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> {
					if (!CollectionUtils.isEmpty(additionalHeaders))
					{
						h.addAll(additionalHeaders);
					}
					h.add(HttpHeaders.AUTHORIZATION, authorization);
				})
				.bodyValue(body)
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while patching data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Posts data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @param typeReference a type reference describing the expected response body type
	 * @param <T>           the response body type
	 * @return @return a mono containing the client response with the response status and headers or an exception
	 */
	public <T> Mono<ResponseEntity<T>> postWithExchange(final String authorization, final URI uri, final Object body,
			final ParameterizedTypeReference<T> typeReference)
	{
		return webClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.exchange()
				.flatMap(response -> response.toEntity(typeReference))
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Posts data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @return @return a mono containing the client response with the response status and headers or an exception
	 */
	public <T> Mono<ResponseEntity<T>> postWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass)
	{
		return webClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.exchange()
				.flatMap(response -> response.toEntity(responseClass))
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Posts data to a specified {@link URI}.
	 *
	 * @param authorization     the authorization value
	 * @param uri               the {@link URI} of the endpoint to make the request to
	 * @param body              the body of the request
	 * @param additionalHeaders a List of additional headers.
	 * @return @return a mono containing the client response with the response status and headers or an exception
	 */
	public <T> Mono<ResponseEntity<T>> postWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass, final MultiValueMap<String, String> additionalHeaders)
	{
		return webClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> {
					if (!CollectionUtils.isEmpty(additionalHeaders))
					{
						h.addAll(additionalHeaders);
					}
					h.add(HttpHeaders.AUTHORIZATION, authorization);
				})
				.bodyValue(body)
				.exchange()
				.flatMap(response -> response.toEntity(responseClass))
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Puts data to a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param body          the body of the request
	 * @return @return a mono containing the client response with the response status and headers or an exception
	 */
	public <T> Mono<ResponseEntity<T>> putWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass)
	{
		return webClient.put()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.bodyValue(body)
				.exchange()
				.flatMap(response -> response.toEntity(responseClass))
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Puts data to a specified {@link URI}.
	 *
	 * @param authorization     the authorization value
	 * @param uri               the {@link URI} of the endpoint to make the request to
	 * @param body              the body of the request
	 * @param additionalHeaders a List of additional headers.
	 * @return @return a mono containing the client response with the response status and headers or an exception
	 */
	public <T> Mono<ResponseEntity<T>> putWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass, final MultiValueMap<String, String> additionalHeaders)
	{
		return webClient.put()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> {
					if (!CollectionUtils.isEmpty(additionalHeaders))
					{
						h.addAll(additionalHeaders);
					}
					h.add(HttpHeaders.AUTHORIZATION, authorization);
				})
				.bodyValue(body)
				.exchange()
				.flatMap(response -> response.toEntity(responseClass))
				.doOnError(e -> LOG.warn("Exception while posting data to {}: {}", uri.getPath(), e))
				.retry(retriesMaxAttempts);
	}

	/**
	 * Deletes data from a specified {@link URI}.
	 *
	 * @param authorization the authorization value
	 * @param uri           the {@link URI} of the endpoint to make the request to
	 * @param responseClass the {@link Class} of the response body type
	 * @param <T>           the response body type
	 * @return a mono containing the converted response body or an exception
	 */
	public <T> Mono<T> delete(final String authorization, final URI uri, final Class<T> responseClass)
	{
		return webClient.delete()
				.uri(uri)
				.headers(h -> h.add(HttpHeaders.AUTHORIZATION, authorization))
				.retrieve()
				.bodyToMono(responseClass)
				.doOnError(e -> LOG.warn("Exception while deleting data from {}: {}", uri.getPath(), e.toString()))
				.retry(retriesMaxAttempts);
	}
}
