package com.hybris.caas.swagger.config;

import com.hybris.caas.swagger.component.SwaggerPathProperties;
import com.hybris.caas.swagger.component.SwaggerPathProperties.SwaggerPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SwaggerWebConfigTest
{
	private SwaggerWebConfig swaggerWebConfig;

	@Mock
	private ResourceHandlerRegistry registry;
	@Mock
	private ResourceHandlerRegistration resourceHandlerRegistration;
	@Mock
	private List<SwaggerPath> configurablePathSegments;
	@Captor
	private ArgumentCaptor<String> pathCaptor;
	@Captor
	private ArgumentCaptor<String> locationCaptor;

	@Test
	public void shouldAddConfigurableResourceHandlers()
	{
		SwaggerPath swaggerPath1 = new SwaggerPath();
		swaggerPath1.setSubDirectory("sub");
		swaggerPath1.setRootDirectory("root");

		SwaggerPath swaggerPath2 = new SwaggerPath();
		swaggerPath1.setSubDirectory("sub2");
		swaggerPath1.setRootDirectory("root2");

		configurablePathSegments = new ArrayList<>();
		configurablePathSegments.add(swaggerPath1);
		configurablePathSegments.add(swaggerPath2);

		SwaggerPathProperties swaggerPathProperties = new SwaggerPathProperties();
		swaggerPathProperties.setSwaggerPaths(configurablePathSegments);

		swaggerWebConfig = new SwaggerWebConfig(swaggerPathProperties);

		when(registry.addResourceHandler(pathCaptor.capture())).thenReturn(resourceHandlerRegistration);
		when(resourceHandlerRegistration.addResourceLocations(locationCaptor.capture())).thenReturn(resourceHandlerRegistration);

		swaggerWebConfig.addResourceHandlers(registry);

		final List<String> pathCaptures = pathCaptor.getAllValues();
		final List<String> locationCaptures = locationCaptor.getAllValues();

		verify(registry).addResourceHandler(pathCaptures.get(0));
		verify(resourceHandlerRegistration).addResourceLocations(locationCaptures.get(0));
		verify(registry).addResourceHandler(pathCaptures.get(1));
		verify(resourceHandlerRegistration).addResourceLocations(locationCaptures.get(1));
		verify(registry).addResourceHandler(pathCaptures.get(2));
		verify(resourceHandlerRegistration).addResourceLocations(locationCaptures.get(2));
		verifyNoMoreInteractions(registry);
	}

	@Test
	public void shouldNotAddConfigurableResourceHandlersWhenNoValuesAreProvided()
	{
		configurablePathSegments = new ArrayList<>();

		SwaggerPathProperties swaggerPathProperties = new SwaggerPathProperties();
		swaggerPathProperties.setSwaggerPaths(configurablePathSegments);

		swaggerWebConfig = new SwaggerWebConfig(swaggerPathProperties);

		when(registry.addResourceHandler(pathCaptor.capture())).thenReturn(resourceHandlerRegistration);
		when(resourceHandlerRegistration.addResourceLocations(locationCaptor.capture())).thenReturn(resourceHandlerRegistration);

		swaggerWebConfig.addResourceHandlers(registry);

		verify(registry).addResourceHandler(pathCaptor.getValue());
		verify(resourceHandlerRegistration).addResourceLocations(locationCaptor.getValue());
		verifyNoMoreInteractions(registry);
	}
}
