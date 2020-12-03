package com.hybris.caas.client.config;

import com.hybris.caas.client.client.CaasWebClient;
import com.hybris.caas.client.client.OauthCaasWebClient;
import com.hybris.caas.client.config.utils.CaasWebClientLogUtils;
import com.hybris.caas.client.config.utils.CaasWebClientProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(CaasWebClientProperties.class)
public class CaasWebClientConfig
{
	private final CaasWebClientProperties caasWebClientProperties;

	public CaasWebClientConfig(final CaasWebClientProperties caasWebClientProperties)
	{
		this.caasWebClientProperties = caasWebClientProperties;
	}

	@Bean
	public CaasWebClient caasWebClient(final WebClient webClient)
	{
		return new OauthCaasWebClient(webClient, caasWebClientProperties);
	}

	/**
	 * A web client  instance is required for spring cloud sleuth integration.
	 * https://cloud.spring.io/spring-cloud-sleuth/reference/html/#webclient
	 *
	 * @return an instance of web client
	 */
	@Bean
	public WebClient createWebClient(final HttpClient httpClient)
	{
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name())
				.filter(CaasWebClientLogUtils.logRequest(caasWebClientProperties.getProperties().getObfuscate().getHeaders()))
				.filter(CaasWebClientLogUtils.logResponse(caasWebClientProperties.getProperties().getObfuscate().getHeaders()))
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public HttpClient defaultHttpClient(ObjectProvider<HttpClientCustomizer> customizers)
	{
		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						caasWebClientProperties.getProperties().getConnectTimeoutMs())
						.doOnConnected(conn -> conn.addHandlerLast(
								new ReadTimeoutHandler(caasWebClientProperties.getProperties().getReadTimeoutMs(),
										TimeUnit.MILLISECONDS))
								.addHandlerLast(new WriteTimeoutHandler(caasWebClientProperties.getProperties().getReadTimeoutMs(),
										TimeUnit.MILLISECONDS))));

		for (HttpClientCustomizer customizer : customizers.orderedStream().collect(Collectors.toList()))
		{
			httpClient = customizer.customize(httpClient);
		}

		return httpClient;
	}
}
