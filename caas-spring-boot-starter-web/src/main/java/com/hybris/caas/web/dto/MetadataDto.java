package com.hybris.caas.web.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@SuppressWarnings("squid:S3437")
public class MetadataDto implements Serializable
{
	public MetadataDto()
	{
		//empty
	}

	private MetadataDto(final OffsetDateTime createdAt, final String createdBy, final OffsetDateTime modifiedAt,
			final String modifiedBy)
	{
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
	}

	public static MetadataDto of(final OffsetDateTime createdAt, final String createdBy, final OffsetDateTime modifiedAt,
			final String modifiedBy)
	{
		return new MetadataDto(createdAt, createdBy, modifiedAt, modifiedBy);
	}

	private OffsetDateTime createdAt;
	private String createdBy;
	private OffsetDateTime modifiedAt;
	private String modifiedBy;

	public OffsetDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(final OffsetDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(final String createdBy)
	{
		this.createdBy = createdBy;
	}

	public OffsetDateTime getModifiedAt()
	{
		return modifiedAt;
	}

	public void setModifiedAt(final OffsetDateTime modifiedAt)
	{
		this.modifiedAt = modifiedAt;
	}

	public String getModifiedBy()
	{
		return modifiedBy;
	}

	public void setModifiedBy(final String modifiedBy)
	{
		this.modifiedBy = modifiedBy;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final MetadataDto metadataDto = (MetadataDto) o;
		return Objects.equals(this.createdAt, metadataDto.createdAt) && Objects.equals(this.createdBy, metadataDto.createdBy)
				&& Objects.equals(this.modifiedAt, metadataDto.modifiedAt) && Objects.equals(this.modifiedBy, metadataDto.modifiedBy);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(createdAt, createdBy, modifiedAt, modifiedBy);
	}

	@Override
	public String toString()
	{
		return "MetadataDto{" + "createdAt=" + createdAt + ", createdBy='" + createdBy + '\'' + ", modifiedAt=" + modifiedAt
				+ ", modifiedBy='" + modifiedBy + '\'' + '}';
	}
}
