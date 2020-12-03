package com.hybris.caas.log.audit.aspect;

import com.hybris.caas.log.audit.annotation.AuditConfigurationChange;
import com.hybris.caas.log.audit.service.AuditLogger;
import com.hybris.caas.log.audit.service.DataChangeObject;
import com.hybris.caas.log.util.JsonNodeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.Id;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationChangeAspectTest
{
	@Mock
	private ProceedingJoinPoint joinPoint;
	@Mock
	private MethodSignature signature;
	@Mock
	private AuditConfigurationChange annotation;
	@Mock
	private AuditLogger auditLogger;

	@Captor
	private ArgumentCaptor<DataChangeObject> captor;

	private ConfigurationChangeAspect aspect;
	private Person person;

	@Before
	public void setUp()
	{
		person = new Person();
		person.setId("generatedId");
		
		when(annotation.objectId()).thenReturn("myId");
		when(annotation.oldValue()).thenReturn("myOldValue");
		when(joinPoint.getSignature()).thenReturn(signature);

		aspect = new ConfigurationChangeAspect(auditLogger);
	}

	@Test
	public void should_handle_create_method() throws Throwable
	{
		when(joinPoint.proceed()).thenReturn(person);
		when(signature.getReturnType()).thenReturn(Person.class);
		when(signature.getParameterNames()).thenReturn(null);

		aspect.storeConfigurationValue(joinPoint, annotation);

		verify(auditLogger).logConfigurationChangeAuditMessage(Mockito.anyString(), Mockito.anyString(), captor.capture());
		assertThat(captor.getValue().getOldObject(), equalTo(JsonNodeUtils.valueToTree(new Object())));
		assertThat(captor.getValue().getNewObject(), equalTo(JsonNodeUtils.valueToTree(person)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_exception_when_null_result_for_update() throws Throwable
	{
		when(joinPoint.proceed()).thenReturn(null);
		when(joinPoint.getArgs()).thenReturn(new Object[] { "id", person });
		when(signature.getReturnType()).thenReturn(Person.class);
		when(signature.getParameterNames()).thenReturn(new String[] { "myId", "myOldValue" });

		aspect.storeConfigurationValue(joinPoint, annotation);
	}

	@Test
	public void should_handle_delete_method() throws Throwable
	{
		when(joinPoint.proceed()).thenReturn(null);
		when(joinPoint.getArgs()).thenReturn(new Object[] { "id", person });
		when(signature.getReturnType()).thenReturn(Void.TYPE);
		when(signature.getParameterNames()).thenReturn(new String[] { "myId", "myOldValue" });

		aspect.storeConfigurationValue(joinPoint, annotation);

		verify(auditLogger).logConfigurationChangeAuditMessage(Mockito.anyString(), Mockito.anyString(), captor.capture());
		assertThat(captor.getValue().getNewObject(), equalTo(JsonNodeUtils.valueToTree(new Object())));
		assertThat(captor.getValue().getOldObject(), equalTo(JsonNodeUtils.valueToTree(person)));
	}

	private static class Person
	{
		@Id
		private String id;
		private String name;

		public String getId()
		{
			return id;
		}

		public void setId(final String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(final String name)
		{
			this.name = name;
		}
	}
}
