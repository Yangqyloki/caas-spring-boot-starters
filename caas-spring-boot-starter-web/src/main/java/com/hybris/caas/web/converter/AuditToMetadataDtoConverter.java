package com.hybris.caas.web.converter;

import com.hybris.caas.data.jpa.audit.Audit;
import com.hybris.caas.security.service.AuthorizationManager;
import com.hybris.caas.security.util.CaasCloudSecurityProperties;
import com.hybris.caas.web.dto.MetadataDto;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts an {@link Audit} object into an {@link MetadataDto} object.
 */
public class AuditToMetadataDtoConverter implements Converter<Audit<String>, MetadataDto>
{
	private final AuthorizationManager authorizationManager;
	private CaasCloudSecurityProperties caasCloudSecurityProperties;

	public AuditToMetadataDtoConverter(final AuthorizationManager authorizationManager,
			final CaasCloudSecurityProperties caasCloudSecurityProperties)
	{
		this.authorizationManager = authorizationManager;
		this.caasCloudSecurityProperties = caasCloudSecurityProperties;
	}

	@Override
	public MetadataDto convert(final Audit<String> audit)
	{
		if (hasAuthorityForMetadata())
		{
			return MetadataDto.of(audit.getCreatedAt(), audit.getCreatedBy(), audit.getModifiedAt(), audit.getModifiedBy());
		}

		return null;
	}

	protected boolean hasAuthorityForMetadata()
	{
		return authorizationManager.hasAuthority(caasCloudSecurityProperties.getManageScope());
	}

}
