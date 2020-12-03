package com.hybris.caas.client.config.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public final class CaasWebClientLogUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(CaasWebClientLogUtils.class);
	private static final Marker HTTP_MARKER = MarkerFactory.getMarker("HTTP");

	protected CaasWebClientLogUtils()
	{
		// Empty constructor.
	}

	public static ExchangeFilterFunction logRequest(final List<String> obfuscateHeaders)
	{
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			final StringBuilder sb = new StringBuilder("\nWeb Client Request: ");
			sb.append("\nUrl: ").append(clientRequest.url().toString());
			sb.append("\nMethod: ").append(clientRequest.method().name());
			sb.append("\nHeaders: ");
			clientRequest.headers().forEach((name, values) -> filterHeaderValues(sb, obfuscateHeaders, name, values));

			LOG.info(HTTP_MARKER, sb.toString());
			return Mono.just(clientRequest);
		});
	}

	public static ExchangeFilterFunction logResponse(final List<String> obfuscateHeaders)
	{
		return ExchangeFilterFunction.ofResponseProcessor(response -> {
			final StringBuilder sb = new StringBuilder("\nWeb Client Response: ");
			sb.append("\nStatus: ").append(response.statusCode().toString());
			sb.append("\nHeaders: ");
			response.headers().asHttpHeaders().forEach((name, values) -> filterHeaderValues(sb, obfuscateHeaders, name, values));
			LOG.info(HTTP_MARKER, sb.toString());

			return Mono.just(response);
		});
	}

	public static void filterHeaderValues(final StringBuilder sb, final List<String> headers, final String name,
			final List<String> values)
	{
		sb.append(name).append(": ");
		if (headers.stream()
				.findAny()
				.filter(f -> f.toLowerCase(Locale.ENGLISH).matches(name.toLowerCase(Locale.ENGLISH)))
				.isPresent())
		{
			sb.append("*****").append(", ");
		}
		else
		{
			values.forEach(v -> sb.append(v).append(", "));
		}
	}
}
