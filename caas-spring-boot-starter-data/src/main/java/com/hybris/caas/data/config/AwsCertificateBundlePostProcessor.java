package com.hybris.caas.data.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Environment post processor that copies the <pre>rds-combined-ca-bundle.pem</pre> from within the starter jar
 * into the default temp directory.
 * And adds an extra property source with the key <code>caas.datasource.sslrootcert</code> pointing to the
 * copied temp <pre>rds-combined-ca-bundle.pem</pre>.
 */
public class AwsCertificateBundlePostProcessor implements EnvironmentPostProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(AwsCertificateBundlePostProcessor.class);
	private static final String CAAS_DATASOURCE_SSLROOTCERT = "caas.datasource.sslrootcert";

	@Override
	public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application)
	{
		final ClassPathResource rdsCertificateBundle = new ClassPathResource("/rds-combined-ca-bundle.pem");

		try (final InputStream inputStream = rdsCertificateBundle.getInputStream())
		{
			//copy classpath:rds-combined-ca-bundle.pem into temp dir
			final Path tempFile = Files.createTempFile("rds-combined-ca-bundle-", ".pem");
			Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			final String tempRdsCertificateBundle = tempFile.toFile().getAbsolutePath();

			// add property source with the absolute path to the temp certificate bundle
			final MutablePropertySources propertySources = environment.getPropertySources();
			final Map<String, Object> properties = new HashMap<>();
			properties.put(CAAS_DATASOURCE_SSLROOTCERT, tempRdsCertificateBundle);
			propertySources.addFirst(new MapPropertySource(AwsCertificateBundlePostProcessor.class.getSimpleName(), properties));

			logger.debug("Added property source with: {}={}", CAAS_DATASOURCE_SSLROOTCERT, tempRdsCertificateBundle);
		}
		catch (IOException e)
		{
			logger.warn("Unable to copy rds certificate bundle", e);
		}
	}
}
