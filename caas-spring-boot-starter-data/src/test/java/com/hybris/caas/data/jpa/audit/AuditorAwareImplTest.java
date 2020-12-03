package com.hybris.caas.data.jpa.audit;

import com.hybris.caas.log.context.UserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
public class AuditorAwareImplTest
{
	@Mock
	private UserProvider userProvider;

	private AuditorAwareImpl auditorAware;

	@Test
	public void should_get_current_auditor()
	{
		Mockito.when(userProvider.getUserId()).thenReturn("abc");
		auditorAware = new AuditorAwareImpl(userProvider);
		assertThat(auditorAware.getCurrentAuditor().get(), equalTo("abc"));
	}

	@Test
	public void should_return_empty_get_current_auditor()
	{
		auditorAware = new AuditorAwareImpl(null);
		assertThat(auditorAware.getCurrentAuditor(), equalTo(Optional.empty()));
	}
}
