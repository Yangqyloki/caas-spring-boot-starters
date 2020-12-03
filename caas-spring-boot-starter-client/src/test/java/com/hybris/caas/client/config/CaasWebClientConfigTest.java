package com.hybris.caas.client.config;

import com.hybris.caas.client.config.utils.CaasWebClientProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaasWebClientConfigTest
{
	@Mock
	private ObjectProvider<HttpClientCustomizer> httpClientCustomizerObjProvider;
	@Mock
	private HttpClientCustomizer httpClientCustomizer1;
	@Mock
	private HttpClientCustomizer httpClientCustomizer2;
	@Mock
	private HttpClient customizedHttpClient;

	private CaasWebClientConfig config = new CaasWebClientConfig(new CaasWebClientProperties());

	@Test
	void should_create_web_client()
	{
		final WebClient webClient = config.createWebClient(HttpClient.create());

		assertThat(webClient).isNotNull();
		webClient.mutate().defaultHeaders(defaultHeaders -> {
			assertThat(defaultHeaders.getAccept()).contains(MediaType.APPLICATION_JSON);
			assertThat(defaultHeaders.getAcceptCharset()).contains(StandardCharsets.UTF_8);
		}).build();
	}

	@Test
	void should_apply_customizers()
	{
		when(httpClientCustomizerObjProvider.orderedStream()).thenReturn(Stream.of(httpClientCustomizer1, httpClientCustomizer2));
		when(httpClientCustomizer1.customize(any(HttpClient.class))).thenReturn(customizedHttpClient);
		when(httpClientCustomizer2.customize(any(HttpClient.class))).thenReturn(customizedHttpClient);

		final HttpClient httpClient = config.defaultHttpClient(httpClientCustomizerObjProvider);

		verify(httpClientCustomizer1).customize(any(HttpClient.class));
		verify(httpClientCustomizer2).customize(any(HttpClient.class));
		assertThat(httpClient).isSameAs(customizedHttpClient);
	}
}
