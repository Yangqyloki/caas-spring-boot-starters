package com.hybris.caas.multitenant.web;

import com.hybris.caas.error.exception.BusinessException;
import com.hybris.caas.multitenant.service.TenantService;
import com.hybris.caas.multitenant.web.util.TenantLock;
import com.hybris.caas.multitenant.web.util.TenantLockImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TenantLockImplTest
{
	private static final String POLAR_BEARS_TENANT = "polarbears";
	private static final int WAIT_TIME_MILLISECONDS = 1000;

	@Mock
	private TenantService tenantService;

	private TenantLock tenantLock;

	@Before
	public void setUp()
	{
		tenantLock = new TenantLockImpl(tenantService, new BusinessException(""));
	}

	@Test
	public void should_throw_exception_if_a_file_transfer_is_already_in_progress_for_the_same_tenant()
	{
		assertFileTransferInProgress(true, POLAR_BEARS_TENANT);
	}

	@Test
	public void should_not_throw_exception_if_a_csv_file_transfer_is_already_in_progress_for_a_different_tenant()
	{
		assertFileTransferInProgress(false, UUID.randomUUID().toString());
	}

	@SuppressWarnings("squid:S2925")
	private void assertFileTransferInProgress(final boolean inProgress, final String tenant)
	{
		final CountDownLatch startSignal = new CountDownLatch(1);
		final AtomicBoolean inProgressFlag = new AtomicBoolean(false);

		final Supplier<Boolean> downloadCountDown = () -> {
			startSignal.countDown();
			try
			{
				sleep(WAIT_TIME_MILLISECONDS);
			}
			catch (final InterruptedException e)
			{
				throw new IllegalStateException("Unexpected interruption of the download/upload test thread.");
			}
			return true;
		};

		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					startSignal.await();
					when(tenantService.getTenant()).thenReturn(POLAR_BEARS_TENANT);

					tenantLock.withLock(downloadCountDown);
				}
				catch (final BusinessException ex)
				{
					inProgressFlag.set(true);
				}
				catch (final InterruptedException e)
				{
					throw new IllegalStateException("Unexpected interruption of the test thread.");
				}
			}
		}.start();

		when(tenantService.getTenant()).thenReturn(tenant);
		tenantLock.withLock(downloadCountDown);

		assertThat(inProgressFlag.get(), is(inProgress));
	}
}
