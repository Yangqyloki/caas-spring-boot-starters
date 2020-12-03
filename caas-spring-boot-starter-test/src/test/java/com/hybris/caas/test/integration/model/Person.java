package com.hybris.caas.test.integration.model;

import com.hybris.caas.web.annotation.Guid;

import javax.validation.constraints.NotEmpty;

public class Person
{
	public static Person ALICE = new Person("Alice");
	public static Person BOB = new Person("Bob", "USid-PERSON-007");
	public static Person NO_NAME = new Person();

	@Guid
	private String id;

	@NotEmpty
	private String name;

	public Person()
	{
		// default constructor
	}

	public Person(final String name)
	{
		this.name = name;
	}

	public Person(final String name, final String id)
	{
		this.name = name;
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

	public String getId()
	{
		return id;
	}

	public void setId(final String id)
	{
		this.id = id;
	}
}
