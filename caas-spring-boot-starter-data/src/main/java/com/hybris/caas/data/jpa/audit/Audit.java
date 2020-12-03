package com.hybris.caas.data.jpa.audit;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Defines the entity audit object.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Audit<U>
{
	public Audit()
	{
		//empty
	}

	private Audit(final OffsetDateTime createdAt, final U createdBy, final OffsetDateTime modifiedAt, final U modifiedBy)
	{
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
	}

	public static <U> Audit<U> of(final OffsetDateTime createdAt, final U createdBy, final OffsetDateTime modifiedAt,
			final U modifiedBy)
	{
		return new Audit<U>(createdAt, createdBy, modifiedAt, modifiedBy);
	}

	@CreatedDate
	@Column(name = AuditDefinition.Column.CREATED_AT)
	protected OffsetDateTime createdAt;

	@CreatedBy
	@Column(name = AuditDefinition.Column.CREATED_BY)
	protected U createdBy;

	@LastModifiedDate
	@Column(name = AuditDefinition.Column.MODIFIED_AT)
	protected OffsetDateTime modifiedAt;

	@LastModifiedBy
	@Column(name = AuditDefinition.Column.MODIFIED_BY)
	protected U modifiedBy;

	public OffsetDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(final OffsetDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public U getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(final U createdBy)
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

	public U getModifiedBy()
	{
		return modifiedBy;
	}

	public void setModifiedBy(final U modifiedBy)
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
		final Audit<?> audit = (Audit<?>) o;
		return Objects.equals(this.createdAt, audit.createdAt) && Objects.equals(this.createdBy, audit.createdBy) && Objects.equals(
				this.modifiedAt, audit.modifiedAt) && Objects.equals(this.modifiedBy, audit.modifiedBy);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(createdAt, createdBy, modifiedAt, modifiedBy);
	}

	@Override
	public String toString()
	{
		return new StringJoiner(", ", Audit.class.getSimpleName() + "[", "]").add("createdAt=" + createdAt)
				.add("createdBy=" + createdBy)
				.add("modifiedAt=" + modifiedAt)
				.add("modifiedBy=" + modifiedBy)
				.toString();
	}
}
