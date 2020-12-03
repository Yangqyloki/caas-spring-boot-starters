package com.hybris.caas.swagger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hybris.caas.swagger.component.SwaggerPathProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

import static com.hybris.caas.swagger.component.SwaggerPathProperties.SwaggerPath;

@ConditionalOnWebApplication
@Configuration
@ComponentScan("com.hybris.caas.swagger")
public class SwaggerWebConfig implements WebMvcConfigurer
{

	public static final String API_PATH_SEGMENT = "api";
	private static final MediaType MEDIA_TYPE_YAML = MediaType.valueOf("text/yaml");
	private static final MediaType MEDIA_TYPE_YML = MediaType.valueOf("text/yml");

	private final List<SwaggerPath> configurablePathSegments;
	private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

	public SwaggerWebConfig(final SwaggerPathProperties swaggerPathProperties)
	{
		configurablePathSegments = swaggerPathProperties.getSwaggerPaths();
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		final MappingJackson2HttpMessageConverter yamlConverter = new MappingJackson2HttpMessageConverter(yamlObjectMapper);
		yamlConverter.setSupportedMediaTypes(Arrays.asList(MEDIA_TYPE_YML, MEDIA_TYPE_YAML));
		converters.add(yamlConverter);
	}

	/**
	 * Configures the static resources mapping required for the Swagger API console.
	 * <p>
	 * Important: Configuration provided in application.yaml for this it is not pick-ed up and
	 * when provided in application.properties it does not work.
	 *
	 * @param registry the resource handlers registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/" + API_PATH_SEGMENT + "/**")
				.addResourceLocations("classpath:/static/" + API_PATH_SEGMENT + "/");

		configurablePathSegments.forEach(swaggerPath -> registry.addResourceHandler("/" + swaggerPath.getSubDirectory() + "/**")
				.addResourceLocations("classpath:/" + swaggerPath.getRootDirectory() + "/" + swaggerPath.getSubDirectory() + "/"));
	}

}
