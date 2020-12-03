package com.hybris.caas.kafka.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

class CaasKafkaEnvPostProcessorTest
{
	private MockEnvironment environment = new MockEnvironment();
	private SpringApplication application = Mockito.mock(SpringApplication.class);

	private CaasKafkaEnvPostProcessor caasKafkaEnvPostProcessor;

	private WireMockServer wireMockServer;

	@BeforeEach
	void setUp()
	{
		wireMockServer = new WireMockServer();
		wireMockServer.start();

		caasKafkaEnvPostProcessor = new CaasKafkaEnvPostProcessor();
	}

	@AfterEach
	void tearDown()
	{
		wireMockServer.stop();
	}

	@Test
	void shouldSetupEnvironment() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException
	{
		var dummyJwtToken = "{\"access_token\":\"a.long.jwt.token.string\",\"token_type\":\"bearer\",\"expires_in\":7776000,\"jti\":\"3f34d19b-d18c-48c8-85a4-31bfba1f6bd5\",\"scope\":\"kafka.none\"}";

		// require caas kafka property to find the service
		environment.setProperty("caas.kafka.vcap-service-name", "kafka");
		// provide properties as if we were on CF via vcap services
		environment.setProperty("vcap.services.kafka.credentials.username", "username");
		environment.setProperty("vcap.services.kafka.credentials.password", "password");
		environment.setProperty("vcap.services.kafka.credentials.urls.ca_cert", "http://localhost:8080/certs/rootCA.crt");
		environment.setProperty("vcap.services.kafka.credentials.urls.token", "http://localhost:8080/oauth/token");

		// @formatter:off
		stubFor(post(urlEqualTo("/oauth/token"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json; charset=utf-8")
						.withBody(dummyJwtToken)));

		stubFor(get(urlEqualTo("/certs/rootCA.crt"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/x-x509-ca-cert; charset=utf-8")
						.withBody(Files.readAllBytes(Paths.get("src/test/resources/dummy.crt")))));
		// @formatter:on

		caasKafkaEnvPostProcessor.postProcessEnvironment(environment, application);

		verify(postRequestedFor(urlEqualTo("/oauth/token")).withBasicAuth(new BasicCredentials("username", "password"))
				.withRequestBody(equalTo("grant_type=client_credentials")));

		final String truststoreLocation = environment.getProperty("caas.kafka.ssl.trust-store-location");
		final String truststorePassword = environment.getProperty("caas.kafka.ssl.trust-store-password");

		assertThat(environment.getProperty("caas.kafka.jaas.username")).isEqualTo("username");
		assertThat(environment.getProperty("caas.kafka.jaas.password")).isEqualTo("a.long.jwt.token.string");
		assertThat(truststoreLocation).isNotNull().startsWith("file:");
		assertThat(truststorePassword).isNotNull().hasSize(16).containsPattern("[a-zA-Z0-9]{16}");
		assertThat(environment.getPropertySources().contains("CaasKafkaEnvPostProcessor")).isTrue();

		final Path truststoreLocationPath = Paths.get(truststoreLocation.replace("file:", ""));
		assertThat(truststoreLocationPath).exists().isRegularFile();

		// assert keystore can be opened with the generated password provided
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		final char[] password = truststorePassword.toCharArray();
		keystore.load(Files.newInputStream(truststoreLocationPath), password); // null creates a new keystore

		assertThat(keystore.containsAlias("sap-cp-kafka-root")).isTrue();
	}

	@Test
	void shouldSkipIfKafkaServiceNameNotProvided()
	{
		caasKafkaEnvPostProcessor.postProcessEnvironment(environment, application);

		assertThat(environment.getProperty("caas.kafka.jaas.username")).isNull();
		assertThat(environment.getProperty("caas.kafka.jaas.password")).isNull();
		assertThat(environment.getProperty("caas.kafka.ssl.trust-store-location")).isNull();
		assertThat(environment.getProperty("caas.kafka.ssl.trust-store-password")).isNull();
		assertThat(environment.getPropertySources().contains("CaasKafkaEnvPostProcessor")).isFalse();
	}

	@Test
	void shouldSkipIfVcapServicesNotProvided()
	{
		environment.setProperty("caas.kafka.vcap-service-name", "kafka");

		caasKafkaEnvPostProcessor.postProcessEnvironment(environment, application);

		assertThat(environment.getProperty("caas.kafka.jaas.username")).isNull();
		assertThat(environment.getProperty("caas.kafka.jaas.password")).isNull();
		assertThat(environment.getProperty("caas.kafka.ssl.trust-store-location")).isNull();
		assertThat(environment.getProperty("caas.kafka.ssl.trust-store-password")).isNull();
		assertThat(environment.getPropertySources().contains("CaasKafkaEnvPostProcessor")).isFalse();
	}
}