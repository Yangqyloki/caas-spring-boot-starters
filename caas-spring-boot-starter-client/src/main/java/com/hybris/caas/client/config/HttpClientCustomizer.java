package com.hybris.caas.client.config;

import reactor.netty.http.client.HttpClient;

/**
 * Callback interface for customizing {@code HttpClient} beans.
 */
@FunctionalInterface
public interface HttpClientCustomizer
{
	/**
	 * Customize the {@link HttpClient}.
	 *
	 * @param httpClient the http client to customize
	 * @return the customized {@link HttpClient}, so calls can be chained.
	 */
	HttpClient customize(HttpClient httpClient);
}
