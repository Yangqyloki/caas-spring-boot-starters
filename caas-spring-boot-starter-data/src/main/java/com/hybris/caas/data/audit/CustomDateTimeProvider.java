package com.hybris.caas.data.audit;

import com.hybris.caas.data.utils.DateUtils;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class CustomDateTimeProvider implements DateTimeProvider
{
	@Override
	public Optional<TemporalAccessor> getNow()
	{
		return Optional.of(DateUtils.offsetDateTimeNowUtc());
	}
}
