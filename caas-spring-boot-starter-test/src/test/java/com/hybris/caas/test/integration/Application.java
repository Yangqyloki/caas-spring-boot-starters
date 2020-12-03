package com.hybris.caas.test.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class Application
{
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}

	/**
	 * A web client  instance is required for spring cloud sleuth integration.
	 * https://cloud.spring.io/spring-cloud-sleuth/reference/html/#webclient
	 *
	 * @return an instance of web client
	 */
	@Bean
	public WebClient createWebClient()
	{
		return WebClient.builder()
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name())
				.build();
	}
}
