package com.hybris.caas.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultAuthorizationManagerTest
{
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;

	private AuthorizationManager authorizationManager = new DefaultAuthorizationManager();

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void beforeEach()
	{
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getAuthorities()).thenReturn((Collection) createAuthorities("test1", "test2"));
	}

	@Nested
	class HasAuthority
	{
		@Test
		void should_return_true_for_valid_scopes()
		{
			final boolean valid = authorizationManager.hasAuthority("test1");
			assertThat(valid).isTrue();
		}

		@Test
		void should_return_false_for_invalid_scopes()
		{
			final boolean valid = authorizationManager.hasAuthority("test3");
			assertThat(valid).isFalse();
		}
	}

	@Nested
	class HasAnyAuthority
	{
		@Test
		void should_return_true_for_all_valid_scopes()
		{
			final boolean valid = authorizationManager.hasAnyAuthority("test1", "test2");
			assertThat(valid).isTrue();
		}

		@Test
		void should_return_true_for_one_valid_scope_and_one_invalid_scope()
		{
			final boolean valid = authorizationManager.hasAnyAuthority("test1", "test3");
			assertThat(valid).isTrue();
		}

		@Test
		void should_return_false_for_AccessDeniedException()
		{
			final boolean valid = authorizationManager.hasAnyAuthority("invalid");
			assertThat(valid).isFalse();
		}

		@Test
		void should_return_false_for_all_invalid_scopes()
		{
			final boolean valid = authorizationManager.hasAnyAuthority("test3", "test4");
			assertThat(valid).isFalse();
		}
	}

	private List<GrantedAuthority> createAuthorities(final String... auths)
	{
		return Arrays.stream(auths).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

}
