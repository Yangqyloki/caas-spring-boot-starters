package com.hybris.caas.web.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebConfigTest
{
	@Mock
	private PathMatchConfigurer pathMatchConfigurer;
	@Mock
	private ContentNegotiationConfigurer contentNegotiationConfigurer;

	private WebConfig webConfig;

	@Before
	public void setUp() throws NoSuchMethodException
	{
		webConfig = new WebConfig();
	}

	@Test
	public void should_disable_suffix_based_path_matching()
	{
		webConfig.configurePathMatch(pathMatchConfigurer);

		verify(pathMatchConfigurer).setUseSuffixPatternMatch(Boolean.FALSE);
	}

	@Test
	public void should_disable_parameter_and_path_extension_content_negociation()
	{
		webConfig.configureContentNegotiation(contentNegotiationConfigurer);

		verify(contentNegotiationConfigurer).favorParameter(false);
		verify(contentNegotiationConfigurer).favorPathExtension(false);
	}
}
