package com.hybris.caas.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * Default implementation of AuthorizationManager, checks the spring security context to ensure that the requested scopes are
 * present.
 */
public class DefaultAuthorizationManager implements AuthorizationManager
{
	@Override
	public boolean hasAuthority(final String authority)
	{
		return hasAnyAuthority(authority);
	}

	@Override
	public boolean hasAnyAuthority(final String... authorities)
	{
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (isNull(authentication))
		{
			return false;
		}

		final Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
		final Set<String> grantedAuthoritySet = AuthorityUtils.authorityListToSet(grantedAuthorities);

		for (final String authority : authorities)
		{
			if (grantedAuthoritySet.contains(authority))
			{
				return true;
			}
		}
		return false;
	}

}
