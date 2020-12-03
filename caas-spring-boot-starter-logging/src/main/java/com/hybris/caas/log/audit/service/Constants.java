package com.hybris.caas.log.audit.service;

/**
 * Constants that can be used by audit log consumers.
 */
final class Constants
{
	private Constants()
	{
		// private constructor
	}

	static final class Channel
	{
		static final String WEB_SERVICE = "web_service";

		private Channel()
		{
			// private constructor
		}
	}

	static final class AuditedObject
	{
		private AuditedObject()
		{
			// private constructor
		}

		static final class AuditedObjectType
		{
			static final String ONLINE_SYSTEM = "online_system";

			private AuditedObjectType()
			{
				// private constructor
			}
		}

		static final class Name
		{
			static final String SAP_HYBRIS_CAAS = "sap_hybris_caas";

			private Name()
			{
				// private constructor
			}
		}

		static final class Identifier
		{
			public static final String NAME = "name";
			public static final String MODULE = "module";
			public static final String OBJECT_TYPE = "objectType";
			public static final String OBJECT_ID = "objectId";

			private Identifier()
			{
				// private constructor
			}
		}
	}

	static final class AuditedDataSubject
	{

		private AuditedDataSubject()
		{
			// private constructor
		}

		static final class Identifier
		{
			static final String ACCOUNT_ID = "account_id";

			private Identifier()
			{
				// private constructor
			}
		}
	}
}
