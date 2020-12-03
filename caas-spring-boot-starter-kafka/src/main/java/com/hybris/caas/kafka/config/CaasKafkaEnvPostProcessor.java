package com.hybris.caas.kafka.config;

import com.hybris.caas.kafka.error.CaasKafkaConfigurationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Environment post processor that handles the connection to SAP CP Kafka Broker backing service
 * By getting the SAP CP Kafka root CA certificate into a generated on the fly SSL trust store with password
 * And getting an OAuth2 token to connect.
 * <p>
 * It provides the following properties to be used in the service configuration
 * <ul>
 *     <li>caas.kafka.jaas.username</li>
 *     <li>caas.kafka.jaas.password</li>
 *     <li>caas.kafka.ssl.trust-store-location</li>
 *     <li>caas.kafka.ssl.trust-store-password</li>
 * </ul>
 * <p>
 * Details:
 * <p>
 * The CA root certificate it's downloaded from <code>${vcap.services.${caas.kafka.vcap-service-name}.credentials.urls.ca_cert}</code>.
 * And a temporary ssl trust store is created with a random password.
 * The location and password are provided as the <code>caas.kafka.ssl.trust-store-location</code> and
 * <code>caas.kafka.ssl.trust-store-location</code> properties.
 * <p>
 * The caas.kafka.jaas.username is the same as <code>${vcap.services.${caas.kafka.vcap-service-name}.credentials.username}</code>>.
 * <p>
 * The caas.kafka.jaas.password is obtained by getting an OAuth2 access token from
 * <code>${vcap.services.<${caas.kafka.vcap-service-name}.credentials.urls.token}</code>.
 */
public class CaasKafkaEnvPostProcessor implements EnvironmentPostProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(CaasKafkaEnvPostProcessor.class);
	private static final String CAAS_KAFKA_JAAS_USERNAME = "caas.kafka.jaas.username";
	private static final String CAAS_KAFKA_JAAS_PASSWORD = "caas.kafka.jaas.password";
	private static final String CAAS_KAFKA_SSL_TRUST_STORE_LOCATION = "caas.kafka.ssl.trust-store-location";
	private static final String CAAS_KAFKA_SSL_TRUST_STORE_PASSWORD = "caas.kafka.ssl.trust-store-password";

	private static final String CAAS_KAFKA_VCAP_SERVICE_NAME = "caas.kafka.vcap-service-name";

	@Override
	public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application)
	{
		var kafkaServiceName = environment.getProperty(CAAS_KAFKA_VCAP_SERVICE_NAME);

		if (Objects.isNull(kafkaServiceName))
		{
			logger.warn("Skipping caas kafka connection setup, no kafka service name provided via {}", CAAS_KAFKA_VCAP_SERVICE_NAME);
			return;
		}

		var username = environment.getProperty(String.format("vcap.services.%s.credentials.username", kafkaServiceName));
		var password = environment.getProperty(String.format("vcap.services.%s.credentials.password", kafkaServiceName));
		var caCertLocation = environment.getProperty(String.format("vcap.services.%s.credentials.urls.ca_cert", kafkaServiceName));
		var tokenUrl = environment.getProperty(String.format("vcap.services.%s.credentials.urls.token", kafkaServiceName));
		var truststorePassword = RandomStringUtils.random(16, true, true);

		if (Objects.isNull(username))
		{
			logger.warn("Skipping caas kafka connection setup, as no kafka service named [{}] found on vcap services!",
					kafkaServiceName);
			return;
		}

		var oauthToken = requestOauthToken(username, password, tokenUrl);
		var caCertFilePath = downloadRootCertificateAuthority(caCertLocation);
		var truststorePath = generateKeystore(truststorePassword, caCertFilePath);

		addPropertySource(environment, username, oauthToken, truststorePassword, truststorePath);
	}

	private String requestOauthToken(final String username, final String password, final String tokenUri)
	{
		final ClientRegistration kafkaClientRegistration = ClientRegistration.withRegistrationId("kafka")
				.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.clientId(username)
				.clientSecret(password)
				.tokenUri(tokenUri)
				.build();

		final AnonymousAuthenticationToken principal = new AnonymousAuthenticationToken("anonymous-key", "kafka",
				List.of(new SimpleGrantedAuthority("anonymous")));

		final OAuth2AuthorizationContext oAuth2Context = OAuth2AuthorizationContext.withClientRegistration(kafkaClientRegistration)
				.principal(principal)
				.build();

		final OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
				.clientCredentials()
				.build();

		return Optional.ofNullable(authorizedClientProvider.authorize(oAuth2Context))
				.map(OAuth2AuthorizedClient::getAccessToken)
				.map(OAuth2AccessToken::getTokenValue)
				.orElseThrow(() -> new IllegalArgumentException("Unable to retrieve OAuth2 access token for Kafka cluster."));
	}

	private Path downloadRootCertificateAuthority(final String caCertLocation)
	{
		try
		{
			final WebClient client = WebClient.create(caCertLocation);
			final Flux<DataBuffer> dataBufferFlux = client.get().retrieve().bodyToFlux(DataBuffer.class);

			final Path rootCAPath = Files.createTempFile("rootCA_", ".crt");
			DataBufferUtils.write(dataBufferFlux, rootCAPath).block();
			return rootCAPath;
		}
		catch (IOException e)
		{
			throw new CaasKafkaConfigurationException("Unable to fetch SAP CP kafka root CA certificate", e);
		}
	}

	private Path generateKeystore(final String keystorePassword, final Path caCertFilePath)
	{
		final Path truststorePath;
		try
		{
			truststorePath = Files.createTempFile("truststore_", ".jks");
		}
		catch (IOException e)
		{
			throw new CaasKafkaConfigurationException("Unable to create truststore jks file", e);
		}

		try (final OutputStream fos = Files.newOutputStream(truststorePath);
				final InputStream input = Files.newInputStream(caCertFilePath))
		{
			// create new keystore in memory
			final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			final char[] password = keystorePassword.toCharArray();
			keystore.load(null, password); // null creates a new keystore

			// add the CA cert into the keystore
			final CertificateFactory factory = CertificateFactory.getInstance("X.509");
			final X509Certificate cert = (X509Certificate) factory.generateCertificate(input);
			keystore.setCertificateEntry("sap-cp-kafka-root", cert);

			// write the keystore into the filesystem
			keystore.store(fos, password);

			return truststorePath;
		}
		catch (Exception e)
		{
			throw new CaasKafkaConfigurationException("Unable to store certificate into truststore", e);
		}
	}

	private void addPropertySource(final ConfigurableEnvironment environment, final String username, final String oauthToken,
			final String truststorePassword, final Path truststorePath)
	{
		final MutablePropertySources propertySources = environment.getPropertySources();
		final Map<String, Object> properties = new HashMap<>();

		properties.put(CAAS_KAFKA_JAAS_USERNAME, username);
		properties.put(CAAS_KAFKA_JAAS_PASSWORD, oauthToken);
		properties.put(CAAS_KAFKA_SSL_TRUST_STORE_LOCATION, "file:" + truststorePath.toFile().getAbsolutePath());
		properties.put(CAAS_KAFKA_SSL_TRUST_STORE_PASSWORD, truststorePassword);

		propertySources.addFirst(new MapPropertySource(CaasKafkaEnvPostProcessor.class.getSimpleName(), properties));

		if (logger.isDebugEnabled())
		{
			logger.debug("Added properties: {}", properties);
		}
	}
}
