package com.hybris.caas.security.service;

/**
 * Authorization manager which allows resource access based on scope
 */
public interface AuthorizationManager
{

	/**
	 * Checks whether the security context has the following granted authority.
	 *
	 * @param authority the granted authority to verify
	 * @return {@code true} if the user has the granted authority; {@code false} otherwise
	 */
	boolean hasAuthority(String authority);

	/**
	 * Checks whether the security context has at least one of the following granted authorities.
	 *
	 * @param authorities the granted authorities to verify - only 1 must apply
	 * @return {@code true} if the user has at least 1 of the granted authorities; {@code false} otherwise
	 */
	boolean hasAnyAuthority(String... authorities);
}