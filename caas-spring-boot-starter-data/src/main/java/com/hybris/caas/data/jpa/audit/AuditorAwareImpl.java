package com.hybris.caas.data.jpa.audit;

import com.hybris.caas.log.context.UserProvider;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Implementation of AuditorAware based on Spring Data.
 */
public class AuditorAwareImpl implements AuditorAware<String>
{
	private final UserProvider userProvider;

	public AuditorAwareImpl(final UserProvider userProvider)
	{
		this.userProvider = userProvider;
	}

	public Optional<String> getCurrentAuditor()
	{
		return Optional.ofNullable(userProvider).map(UserProvider::getUserId);
	}
}
