package com.hybris.caas.client.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hybris.caas.client.config.utils.CaasWebClientProperties;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class OauthCaasWebClient extends CaasWebClient
{
	private static final Logger LOG = LoggerFactory.getLogger(OauthCaasWebClient.class);

	private static final String BEARER = "Bearer %s";
	private static final String BASIC = "Basic %s";

	private final Cache<String, String> tokenCache;
	private final String clientId;
	private final String clientSecret;
	private final String tokenUriTemplate;

	public OauthCaasWebClient(final WebClient webClient, final CaasWebClientProperties caasWebClientProperties)
	{
		super(webClient, caasWebClientProperties.getProperties().getRetry().getMaxAttempts());

		this.clientId = caasWebClientProperties.getUaa().getClientId();
		this.clientSecret = caasWebClientProperties.getUaa().getClientSecret();
		this.tokenUriTemplate = caasWebClientProperties.getUaa().getTokenUriTemplate();

		this.tokenCache = Caffeine.newBuilder()
				.expireAfterWrite(caasWebClientProperties.getTokenCache().getExpireAfterWrite(), TimeUnit.HOURS)
				.maximumSize(caasWebClientProperties.getTokenCache().getMaxSize())
				.build();
	}

	@Override
	public <T> Mono<T> get(final String authorization, final URI uri, final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(token -> super.get(String.format(BEARER, token), uri, responseClass));
	}

	@Override
	public <T> Mono<T> get(final String authorization, final URI uri, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return getToken(authorization).flatMap(
				token -> super.get(String.format(BEARER, token), uri, responseClass, additionalHeaders));
	}

	@Override
	public <T> Mono<T> post(final String authorization, final URI uri, final Object body, final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(token -> super.post(String.format(BEARER, token), uri, body, responseClass));
	}

	@Override
	public <T> Mono<T> post(final String authorization, final URI uri, final Object body, final ParameterizedTypeReference<T> typeReference)
	{
		return getToken(authorization).flatMap(token -> super.post(String.format(BEARER, token), uri, body, typeReference));
	}

	@Override
	public <T> Mono<T> post(final String authorization, final URI uri, final Object body, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return getToken(authorization).flatMap(
				token -> super.post(String.format(BEARER, token), uri, body, responseClass, additionalHeaders));
	}

	@Override
	public <T> Mono<ResponseEntity<T>> postWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(
				token -> super.postWithExchange(String.format(BEARER, token), uri, body, responseClass));
	}

	@Override
	public <T> Mono<ResponseEntity<T>> postWithExchange(final String authorization, final URI uri, final Object body,
			final ParameterizedTypeReference<T> typeReference)
	{
		return getToken(authorization).flatMap(
				token -> super.postWithExchange(String.format(BEARER, token), uri, body, typeReference));
	}

	@Override
	public <T> Mono<ResponseEntity<T>> postWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass, final MultiValueMap<String, String> additionalHeaders)
	{
		return getToken(authorization).flatMap(
				token -> super.postWithExchange(String.format(BEARER, token), uri, body, responseClass, additionalHeaders));
	}

	@Override
	public <T> Mono<T> put(final String authorization, final URI uri, final Object body, final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(token -> super.put(String.format(BEARER, token), uri, body, responseClass));
	}

	@Override
	public <T> Mono<T> put(final String authorization, final URI uri, final Object body, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return getToken(authorization).flatMap(
				token -> super.put(String.format(BEARER, token), uri, body, responseClass, additionalHeaders));
	}

	@Override
	public <T> Mono<ResponseEntity<T>> putWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(
				token -> super.putWithExchange(String.format(BEARER, token), uri, body, responseClass));
	}

	@Override
	public <T> Mono<ResponseEntity<T>> putWithExchange(final String authorization, final URI uri, final Object body,
			final Class<T> responseClass, final MultiValueMap<String, String> additionalHeaders)
	{
		return getToken(authorization).flatMap(
				token -> super.putWithExchange(String.format(BEARER, token), uri, body, responseClass, additionalHeaders));
	}

	@Override
	public <T> Mono<T> patch(final String authorization, final URI uri, final Object body, final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(token -> super.patch(String.format(BEARER, token), uri, body, responseClass));
	}

	@Override
	public <T> Mono<T> patch(final String authorization, final URI uri, final Object body, final Class<T> responseClass,
			final MultiValueMap<String, String> additionalHeaders)
	{
		return getToken(authorization).flatMap(
				token -> super.patch(String.format(BEARER, token), uri, body, responseClass, additionalHeaders));
	}

	@Override
	public <T> Mono<T> delete(final String authorization, final URI uri, final Class<T> responseClass)
	{
		return getToken(authorization).flatMap(token -> super.delete(String.format(BEARER, token), uri, responseClass));
	}

	private Mono<String> getToken(final String tenantId)
	{
		return CacheMono.lookup(key -> Mono.justOrEmpty(tokenCache.getIfPresent(key)).map(Signal::next), tenantId)
				.onCacheMissResume(getTokenInternal(tenantId))
				.andWriteWith((key, signal) -> Mono.fromRunnable(
						() -> Optional.ofNullable(signal.get()).ifPresent(value -> tokenCache.put(key, value))));
	}

	private Mono<String> getTokenInternal(final String tenantId)
	{
		return Mono.defer(() -> super.get(getBasicAuth(), getUAAUri(tenantId), String.class)).map(response -> {
			LOG.debug("getToken for tenantId '{}' returned: {}", tenantId, response);
			final ReadContext readContext = JsonPath.using(
					Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS))
					.parse(response);
			final String token = readContext.read("$.access_token");
			Assert.notNull(token, "Did not receive token from XSUAA");
			return token;
		});
	}

	private URI getUAAUri(final String tenantId)
	{
		return UriComponentsBuilder.fromUriString(String.format(tokenUriTemplate, tenantId)).build().toUri();
	}

	private String getBasicAuth()
	{
		final byte[] bytes = (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8);
		final String base64 = new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
		return String.format(BASIC, base64);
	}
}
