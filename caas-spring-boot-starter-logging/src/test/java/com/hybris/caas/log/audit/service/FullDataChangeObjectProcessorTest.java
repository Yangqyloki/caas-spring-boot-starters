package com.hybris.caas.log.audit.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FullDataChangeObjectProcessorTest
{
	private final FullDataChangeObjectProcessor processor = new FullDataChangeObjectProcessor();

	@Mock
	private DataChangeValueAppender appenderFunction;

	private Person alice;
	private Person bob;
	private DataChangeObject dataChangeObject;

	@Before
	public void setUp()
	{
		alice = new Person();
		alice.setId("id");
		alice.setName("Alice");
		alice.setPhone("phone-1");

		bob = new Person();
		bob.setId("id");
		bob.setName("Bob");
		bob.setPhone("phone-2");

		dataChangeObject = DataChangeObject.withOldValue(alice).setNewValue(bob);
	}

	@Test
	public void should_append_value()
	{
		processor.process(dataChangeObject, appenderFunction);

		verify(appenderFunction).appendValues("value", "{\"id\":\"id\",\"name\":\"Alice\",\"phone\":\"phone-1\"}", "{\"id\":\"id\",\"name\":\"Bob\",\"phone\":\"phone-2\"}");
	}

	private static class Person
	{
		private String id;
		private String name;
		private String phone;

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

		public String getPhone()
		{
			return phone;
		}

		public void setPhone(final String phone)
		{
			this.phone = phone;
		}
	}
}
