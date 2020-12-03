package com.hybris.caas.data.jpa.audit;

/**
 * Defines the name of the columns for the {@link Audit} entity.
 */
public final class AuditDefinition
{
	private AuditDefinition()
	{
		// empty
	}

	public static final class Column
	{
		public static final String CREATED_AT = "created_at";
		public static final String CREATED_BY = "created_by";
		public static final String MODIFIED_AT = "modified_at";
		public static final String MODIFIED_BY = "modified_by";

		private Column()
		{
			//empty
		}
	}
}

