package com.hybris.caas.log.audit.service;

import org.springframework.util.Assert;

/**
 * Data transfer object to be used when audit logging data access audit messages and data modifications audit messages.
 * To be used to contain the information about the audited data subject.
 * <p>
 * Example Usage:
 * <pre>
 * DataSubject.of("owiegfs483ov6tgeow86ifgaor7tvgz", DataSubject.Role.MERCHANT)
 * <pre>
 */
public class DataSubject
{
	private final String accountId;
	private final String role;

	DataSubject(final String accountId, final Role role)
	{
		this.accountId = accountId;
		this.role = role.getValue();
	}

	public String getAccountId()
	{
		return accountId;
	}

	public String getRole()
	{
		return role;
	}

	public static DataSubject of(final String accountId, final Role role)
	{
		Assert.notNull(accountId, "Account ID cannot be null.");
		Assert.notNull(role, "Role cannot be null.");

		return new DataSubject(accountId, role);
	}

	/**
	 * Static list of audited data subject roles.
	 */
	public enum Role
	{
		ADMINISTRATOR("Administrator"), MERCHANT("Merchant"), DEVELOPER("Developer"), CUSTOMER("Customer");

		private String value;

		Role(final String value)
		{
			this.value = value;
		}

		public String getValue()
		{
			return this.value;
		}
	}
}
