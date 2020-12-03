package com.hybris.caas.swagger.component;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates configuration properties required for static path access in swagger.
 */
@Component
@ConfigurationProperties(prefix = "swagger")
@Validated
public class SwaggerPathProperties
{
	private List<SwaggerPath> swaggerPaths = new ArrayList<>();

	public List<SwaggerPath> getSwaggerPaths()
	{
		return swaggerPaths;
	}

	public void setSwaggerPaths(final List<SwaggerPath> swaggerPaths)
	{
		this.swaggerPaths = swaggerPaths;
	}

	public static class SwaggerPath
	{
		@NotEmpty
		private String rootDirectory;
		@NotEmpty
		private String subDirectory;

		public String getRootDirectory()
		{
			return rootDirectory;
		}

		public void setRootDirectory(final String rootDirectory)
		{
			this.rootDirectory = rootDirectory;
		}

		public String getSubDirectory()
		{
			return subDirectory;
		}

		public void setSubDirectory(final String subDirectory)
		{
			this.subDirectory = subDirectory;
		}
	}
}
