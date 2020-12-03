package com.hybris.caas.web.converter;

import com.hybris.caas.data.jpa.audit.Audit;
import com.hybris.caas.data.utils.DateUtils;
import com.hybris.caas.security.service.AuthorizationManager;
import com.hybris.caas.security.util.CaasCloudSecurityProperties;
import com.hybris.caas.web.dto.MetadataDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditToMetadataDtoConverterTest
{
	@Mock
	private AuthorizationManager authorizationManager;

	private CaasCloudSecurityProperties caasCloudSecurityProperties;

	private AuditToMetadataDtoConverter converter;

	private Audit audit;

	@BeforeEach
	public void setUp()
	{
		caasCloudSecurityProperties = new CaasCloudSecurityProperties();
		final OffsetDateTime createdAt = DateUtils.offsetDateTimeNowUtc();
		audit = Audit.of(createdAt, "user123", createdAt, "user456");
		converter = new AuditToMetadataDtoConverter(authorizationManager, caasCloudSecurityProperties);
	}

	@Test
	void should_convert()
	{
		when(authorizationManager.hasAuthority(any())).thenReturn(Boolean.TRUE);

		final MetadataDto dto = converter.convert(audit);
		assertThat(dto.getCreatedAt(), equalTo(audit.getCreatedAt()));
		assertThat(dto.getCreatedBy(), equalTo(audit.getCreatedBy()));
		assertThat(dto.getModifiedAt(), equalTo(audit.getModifiedAt()));
		assertThat(dto.getModifiedBy(), equalTo(audit.getModifiedBy()));
	}

	@Test
	void should_not_convert_when_user_not_authenticated()
	{
		when(authorizationManager.hasAuthority(any())).thenReturn(Boolean.FALSE);

		final MetadataDto dto = converter.convert(audit);
		assertThat(dto, is(nullValue()));
	}
}
