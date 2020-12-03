package com.hybris.caas.swagger.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hybris.caas.swagger.ApiDescriptionDto;
import com.hybris.caas.swagger.ApiDescriptionsDto;
import com.hybris.caas.swagger.config.SwaggerWebConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(Enclosed.class)
public class SwaggerControllerTest
{
	@RunWith(SpringRunner.class)
	@WebMvcTest(SwaggerController.class)
	@Import(SwaggerWebConfig.class)
	@TestPropertySource(properties = "caas.swagger.rewrite-servers-url:false")
	public static class DoNotRewriteServersUrl
	{
		// these values comes from src/test/resources/api-description.yaml!/servers.url
		static final String ORIGINAL_URL = "http://somewhere.com/my-service";
		static final String SECOND_ORIGINAL_URL = "http://somewhereelse.com/my-service";

		@Autowired
		private MockMvc mockMvc;

		private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

		@Test
		public void shouldKeepServerUrlFromApiDescriptionYamlFile() throws Exception
		{
			final String apiDescriptionRawYaml = mockMvc.perform(get("/api-description.yaml"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith("text/yaml"))
					.andReturn()
					.getResponse()
					.getContentAsString();

			final JsonNode apiDescription = yamlObjectMapper.readTree(apiDescriptionRawYaml);
			final JsonNode serversNode = apiDescription.get("servers");

			assertThat(serversNode, is(notNullValue()));
			assertThat(serversNode.isArray(), is(true));
			assertThat(serversNode.size(), is(2));
			assertThat(serversNode.get(0).get("url").asText(), equalTo(ORIGINAL_URL));
			assertThat(serversNode.get(1).get("url").asText(), equalTo(SECOND_ORIGINAL_URL));
		}
	}

	@RunWith(SpringRunner.class)
	@WebMvcTest(SwaggerController.class)
	@Import({ SwaggerWebConfig.class, TestSwaggerWebConfig.class })
	public static class RewriteServersUrl
	{
		@Autowired
		private MockMvc mockMvc;

		private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

		@Test
		public void shouldReplaceServersUrlWithCurrentHost() throws Exception
		{
			final String apiDescriptionRawYaml = mockMvc.perform(get("/api-description.yaml"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith("text/yaml"))
					.andReturn()
					.getResponse()
					.getContentAsString();

			final JsonNode apiDescription = yamlObjectMapper.readTree(apiDescriptionRawYaml);
			final JsonNode serversNode = apiDescription.get("servers");

			assertThat(serversNode, is(notNullValue()));
			assertThat(serversNode.isArray(), is(true));
			assertThat(serversNode.size(), is(1));
			assertThat(serversNode.get(0).get("url").asText(), equalTo("http://localhost"));
		}

		@Test
		public void shouldReplaceServersUrlWithProxyHost() throws Exception
		{
			final String apiDescriptionRawYaml = mockMvc.perform(
					get("/api-description.yaml").header("X-Forwarded-Proto", "https").header("X-Forwarded-Host", "my.external.host"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith("text/yaml"))
					.andReturn()
					.getResponse()
					.getContentAsString();

			final JsonNode apiDescription = yamlObjectMapper.readTree(apiDescriptionRawYaml);
			final JsonNode serversNode = apiDescription.get("servers");

			assertThat(serversNode, is(notNullValue()));
			assertThat(serversNode.isArray(), is(true));
			assertThat(serversNode.size(), is(1));
			assertThat(serversNode.get(0).get("url").asText(), equalTo("https://my.external.host"));
		}

		@Test
		public void shouldReplaceServersUrlWithProxyHostAndPath() throws Exception
		{
			final String apiDescriptionRawYaml = mockMvc.perform(get("/api-description.yaml").header("X-Forwarded-Proto", "https")
					.header("X-Forwarded-Host", "my.external.host")
					.header("X-Forwarded-Prefix", "path"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentTypeCompatibleWith("text/yaml"))
					.andReturn()
					.getResponse()
					.getContentAsString();

			final JsonNode apiDescription = yamlObjectMapper.readTree(apiDescriptionRawYaml);
			final JsonNode serversNode = apiDescription.get("servers");

			assertThat(serversNode, is(notNullValue()));
			assertThat(serversNode.isArray(), is(true));
			assertThat(serversNode.size(), is(1));
			assertThat(serversNode.get(0).get("url").asText(), equalTo("https://my.external.host/path"));
		}
	}

	@RunWith(SpringRunner.class)
	@WebMvcTest(SwaggerController.class)
	@Import({ SwaggerWebConfig.class, TestSwaggerWebConfig.class })
	public static class GetApiDocsList
	{
		@Autowired
		private MockMvc mockMvc;

		private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

		@Test
		public void shouldReturnTwoApiDocs() throws Exception
		{
			final String apiDocs = mockMvc.perform(get("/apidocs"))
					.andDo(print())
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			final JsonNode apiDescriptionsDto = yamlObjectMapper.readTree(apiDocs);
			final List<ApiDescriptionDto> apiDescriptions = yamlObjectMapper.convertValue(apiDescriptionsDto.get("apiDescriptions"),
					new TypeReference<List<ApiDescriptionDto>>()
					{
					});

			assertThat(apiDescriptions, is(notNullValue()));
			assertThat(apiDescriptions.size(), is(2));
			assertThat(apiDescriptions, containsInAnyOrder(
					allOf(hasProperty("name", is("Service API Example")), hasProperty("url", is("../api-description.yaml"))),
					allOf(hasProperty("name", is("Test Module API Example")),
							hasProperty("url", is("../test-api-description.yaml")))));

		}
	}

	@Configuration
	static class TestSwaggerWebConfig
	{
		@Bean
		public ForwardedHeaderFilter forwardedHeaderFilter()
		{
			return new ForwardedHeaderFilter();
		}
	}
}
