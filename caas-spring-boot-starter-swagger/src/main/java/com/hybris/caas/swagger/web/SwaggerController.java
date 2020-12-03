package com.hybris.caas.swagger.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hybris.caas.swagger.ApiDescriptionDto;
import com.hybris.caas.swagger.ApiDescriptionsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RestController
public class SwaggerController
{
	private static final String API_DESCRIPTION_REGEX = "*api-description.yaml";
	private static final String SLASH = "/";
	private static final String TEXT_YAML = "text/yaml";

	@Value("${caas.swagger.rewrite-servers-url:true}")
	private boolean rewriteServersUrlEntry;

	private final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

	private final Map<String, Function<HttpServletRequest, Map<String, Object>>> apiDescriptionsToFunctions = new HashMap<>();
	private final List<ApiDescriptionDto> apiDocs = new ArrayList<>();

	@PostConstruct
	public void load() throws IOException
	{
		final Resource[] resources = getAPIDescriptions();
		populateApiDocsList(resources);
		populateApiDescriptionsToFunctions(resources);
	}

	public void loadApiDescription(String apiDescriptionFileName)
	{
		final Map<String, Object> apiDescription;
		Function<HttpServletRequest, Map<String, Object>> apiDescriptionFunction;
		try (final InputStream apiDescriptionStream = getClass().getResourceAsStream(SLASH + apiDescriptionFileName))
		{
			apiDescription = yamlObjectMapper.readValue(apiDescriptionStream, new TypeReference<Map<String, Object>>()
			{
			});
		}
		catch (final IOException e)
		{
			throw new IllegalArgumentException("Invalid API description file '" + apiDescriptionFileName + "'.", e);
		}

		if (rewriteServersUrlEntry)
		{
			apiDescriptionFunction = request -> {
				final URI uri = ServletUriComponentsBuilder.fromServletMapping(request).build().toUri();
				final Map<String, String> serverUrlEntry = Collections.singletonMap("url", uri.toString());
				apiDescription.put("servers", Collections.singletonList(serverUrlEntry));

				return apiDescription;
			};
		}
		else
		{
			apiDescriptionFunction = request -> apiDescription;
		}

		apiDescriptionsToFunctions.put(apiDescriptionFileName, apiDescriptionFunction);

	}

	@GetMapping(value = "/*api-description.yaml", produces = TEXT_YAML)
	public Object apiDocs(final HttpServletRequest request)
	{
		final String fileName = request.getRequestURI().substring(request.getRequestURI().lastIndexOf(SLASH) + 1);
		return Optional.ofNullable(apiDescriptionsToFunctions.get(fileName)).map(function -> function.apply(request)).orElse(null);
	}

	@GetMapping(value = "/apidocs")
	public ApiDescriptionsDto apiDocsList()
	{
		return ApiDescriptionsDto.of(apiDocs);
	}

	/**
	 * Iterate through the list of resources and populate the {@link ApiDescriptionDto} containing the list of API Description Documents.
	 */
	private void populateApiDocsList(final Resource[] resources) throws IOException
	{
		for (Resource resource : resources)
		{
			final File file = resource.getFile();
			final ObjectMapper om = new ObjectMapper(new YAMLFactory());
			final Map yaml = (Map) om.readValue(file, Object.class);

			apiDocs.add(ApiDescriptionDto.builder()
					.name(((Map) yaml.get("info")).get("title").toString())
					.url(".." + SLASH + resource.getFilename())
					.build());
		}
	}

	/**
	 * Iterate through the list of resources and populate the API description function for each API description resource.
	 */
	private void populateApiDescriptionsToFunctions(Resource[] resources)
	{
		for (Resource resource : resources)
		{
			loadApiDescription(resource.getFilename());
		}
	}

	private Resource[] getAPIDescriptions()
	{
		try
		{
			final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			return resolver.getResources("classpath*:" + SLASH + API_DESCRIPTION_REGEX);
		}
		catch (final IOException e)
		{
			throw new IllegalArgumentException("Cannot find API description file matching the regex '" + API_DESCRIPTION_REGEX + "'.",
					e);
		}
	}

}
