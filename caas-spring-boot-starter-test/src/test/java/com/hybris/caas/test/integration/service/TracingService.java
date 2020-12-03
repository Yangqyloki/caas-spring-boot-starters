package com.hybris.caas.test.integration.service;

import brave.Tracing;
import com.hybris.caas.test.integration.controller.TracingController;
import com.hybris.caas.test.integration.util.TracingAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Async
@Service
public class TracingService
{
	private static final Logger LOG = LoggerFactory.getLogger(TracingService.class);
	private final Tracing tracing;

	public TracingService(final Tracing tracing)
	{
		this.tracing = tracing;
	}

	public void execute()
	{
		LOG.info("In async service");
		TracingAssertions.assertUserTraceContext(tracing);
		TracingController.queue.offer(MDC.getCopyOfContextMap());
	}

}
