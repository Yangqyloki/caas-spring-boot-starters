package com.hybris.caas.swagger.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class SwaggerRedirectControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private Environment environment;

	@Before
	public void setUp()
	{

		when(environment.getProperty(any())).thenReturn(null);
		mockMvc = MockMvcBuilders.standaloneSetup(new SwaggerRedirectController(environment))
				.addFilters(new ForwardedHeaderFilter())
				.build();
	}

	@Test
	public void shouldRedirectUsingSameHost() throws Exception
	{
		mockMvc.perform(get("/"))
				.andDo(print())
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("http://localhost/api/index.html"));
	}

	@Test
	public void shouldRedirectFollowingXForwardedHeaders() throws Exception
	{

		mockMvc.perform(get("/").header("X-Forwarded-Host", "my.external.host").header("X-Forwarded-Proto", "https"))
				.andDo(print())
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("https://my.external.host/api/index.html"));
	}

	@Test
	public void shouldRedirectFollowingXForwardedHeadersIncludingPrefix() throws Exception
	{

		mockMvc.perform(get("/").header("X-Forwarded-Host", "my.external.host")
				.header("X-Forwarded-Proto", "https")
				.header("X-Forwarded-Prefix", "my-context-path"))
				.andDo(print())
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("https://my.external.host/my-context-path/api/index.html"));
	}

	@Test
	public void shouldAddPrefixToRedirectPathWhenK8PropertyIsSet() throws Exception
	{
		when(environment.getProperty(any())).thenReturn("testPrefix");
		MockMvc mockPrefixMvc = MockMvcBuilders.standaloneSetup(new SwaggerRedirectController(environment))
				.addFilters(new ForwardedHeaderFilter())
				.build();

		mockPrefixMvc.perform(get("/").header("X-Forwarded-Host", "my.external.host").header("X-Forwarded-Proto", "https"))
				.andDo(print())
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("https://my.external.host/testPrefix/api/index.html"));
	}
}
