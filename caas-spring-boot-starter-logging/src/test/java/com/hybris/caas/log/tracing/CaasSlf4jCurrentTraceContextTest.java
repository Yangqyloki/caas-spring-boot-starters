package com.hybris.caas.log.tracing;

import brave.Span;
import brave.Tracing;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig.SingleBaggageField;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.CurrentTraceContext.Scope;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.sap.hcp.cf.logging.common.Fields;
import com.sap.hcp.cf.logging.common.request.HttpHeaders;
import org.junit.After;
import org.junit.Test;
import org.slf4j.MDC;

import static com.hybris.caas.log.tracing.Constants.X_B3_SPAN_ID;
import static com.hybris.caas.log.tracing.Constants.X_B3_TRACE_ID;
import static com.sap.hcp.cf.logging.common.request.HttpHeaders.CORRELATION_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class CaasSlf4jCurrentTraceContextTest
{
	private final CurrentTraceContext currentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
			.addScopeDecorator(StrictScopeDecorator.create())
			.build();
	private final CaasSlf4jCurrentTraceContext slf4jCurrentTraceContext = new CaasSlf4jCurrentTraceContext();

	private final Tracing tracing = Tracing.newBuilder()
			.currentTraceContext(currentTraceContext)
			.propagationFactory(BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
					.add(SingleBaggageField.local(BaggageField.create(Fields.TENANT_ID)))
					.add(SingleBaggageField.remote(BaggageField.create(HttpHeaders.CORRELATION_ID.getName())))
					.build())
			.build();

	private final Span span = this.tracing.tracer().nextSpan().name("span").start();

	@After
	public void cleanMdc()
	{
		MDC.clear();
	}

	@Test
	public void should_set_entries_to_mdc_from_span()
	{
		assertFieldsEmpty();
		assertBaggageEmpty();
		populateMdc();
		populateBaggage();
		populatePropagationKeys();

		final Span child = this.tracing.tracer().newChild(this.span.context()).name("parent").start();
		final Scope scope = this.slf4jCurrentTraceContext.decorateScope(child.context(),
				currentTraceContext.newScope(child.context()));

		assertFieldsPopulated();
		assertBaggagePopulated();
		assertPropagationKeysPopulated();

		scope.close();
		assertThat(MDC.get(X_B3_TRACE_ID)).isNullOrEmpty();
		assertFieldsPopulated();
	}

	@Test
	public void should_remove_entries_from_mdc_from_null_span()
	{
		MDC.put(X_B3_TRACE_ID, "A");
		MDC.put(X_B3_SPAN_ID, "B");

		assertFieldsEmpty();
		assertBaggageEmpty();

		final Scope scope = this.slf4jCurrentTraceContext.decorateScope(null, currentTraceContext.newScope(null));

		assertFieldsEmpty();
		assertBaggageEmpty();

		scope.close();

		assertFieldsEmpty();
		assertBaggageEmpty();
	}

	private void populateMdc()
	{
		MDC.put(Fields.COMPONENT_ID, "caas-application-id");
		MDC.put(Fields.COMPONENT_NAME, "caas-application");
		MDC.put(Fields.SPACE_ID, "caas-space-id");
		MDC.put(Fields.SPACE_NAME, "caas-space");
		MDC.put(Fields.COMPONENT_INSTANCE, "1");
		MDC.put(Fields.COMPONENT_TYPE, "application");
		MDC.put(Fields.CONTAINER_ID, "caas-container");
	}

	private void populateBaggage()
	{
		BaggageField.getByName(span.context(), Fields.TENANT_ID).updateValue(span.context(), "my-tenant");
	}

	private void populatePropagationKeys()
	{
		BaggageField.getByName(span.context(), CORRELATION_ID.getName()).updateValue(span.context(), "123");
	}

	private void assertFieldsEmpty()
	{
		assertThat(MDC.get(Fields.ORGANIZATION_ID)).isNullOrEmpty();
		assertThat(MDC.get(Fields.ORGANIZATION_NAME)).isNullOrEmpty();
		assertThat(MDC.get(Fields.SPACE_ID)).isNullOrEmpty();
		assertThat(MDC.get(Fields.SPACE_NAME)).isNullOrEmpty();
		assertThat(MDC.get(Fields.CONTAINER_ID)).isNullOrEmpty();
		assertThat(MDC.get(Fields.COMPONENT_ID)).isNullOrEmpty();
		assertThat(MDC.get(Fields.COMPONENT_NAME)).isNullOrEmpty();
		assertThat(MDC.get(Fields.COMPONENT_INSTANCE)).isNullOrEmpty();
		assertThat(MDC.get(Fields.COMPONENT_TYPE)).isNullOrEmpty();
	}

	private void assertFieldsPopulated()
	{
		assertThat(MDC.get(Fields.ORGANIZATION_ID)).isEqualTo("-");
		assertThat(MDC.get(Fields.ORGANIZATION_NAME)).isEqualTo("-");
		assertThat(MDC.get(Fields.SPACE_ID)).isEqualTo("caas-space-id");
		assertThat(MDC.get(Fields.SPACE_NAME)).isEqualTo("caas-space");
		assertThat(MDC.get(Fields.CONTAINER_ID)).isEqualTo("caas-container");
		assertThat(MDC.get(Fields.COMPONENT_TYPE)).isEqualTo("application");
		assertThat(MDC.get(Fields.COMPONENT_ID)).isEqualTo("caas-application-id");
		assertThat(MDC.get(Fields.COMPONENT_NAME)).isEqualTo("caas-application");
		assertThat(MDC.get(Fields.COMPONENT_INSTANCE)).isEqualTo("1");
	}

	private void assertBaggageEmpty()
	{
		assertThat(MDC.get(Fields.TENANT_ID)).isNullOrEmpty();
	}

	private void assertBaggagePopulated()
	{
		assertThat(MDC.get(Fields.TENANT_ID)).isEqualTo("my-tenant");
	}

	private void assertPropagationKeysPopulated()
	{
		assertThat(MDC.get(HttpHeaders.CORRELATION_ID.getField())).isEqualTo("123");
	}
}
