package com.hybris.caas.log.context;

/**
 * Provides information about the user.
 */
public interface UserProvider
{
	/**
	 * Gets the user identifier.
	 *
	 * @return the user identifier
	 */
	String getUserId();

	/**
	 * Gets the tenant identifier.
	 *
	 * @return the tenant identifier
	 */
	String getTenant();

	/**
	 * Gets the tenant subaccount's identifier.
	 *
	 * @return the tenant subaccount's identifier
	 */
	String getSubaccountId();

	/**
	 * Gets the user's client IP address.
	 *
	 * @return the client IP address.
	 */
	String getClientIp();
}
