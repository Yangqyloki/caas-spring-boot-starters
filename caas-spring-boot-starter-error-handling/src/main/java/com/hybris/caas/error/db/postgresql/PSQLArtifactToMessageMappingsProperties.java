package com.hybris.caas.error.db.postgresql;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "caas.psql")
public class PSQLArtifactToMessageMappingsProperties
{
	private Map<String, String> constraintToMessageMappings = Collections.emptyMap();

	public Map<String, String> getConstraintToMessageMappings()
	{
		return constraintToMessageMappings;
	}

	public void setConstraintToMessageMappings(final Map<String, String> constraintToMessageMappings)
	{
		this.constraintToMessageMappings = constraintToMessageMappings;
	}
}
