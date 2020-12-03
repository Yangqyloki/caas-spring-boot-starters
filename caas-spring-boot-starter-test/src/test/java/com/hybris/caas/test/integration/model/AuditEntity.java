package com.hybris.caas.test.integration.model;

import javax.persistence.Id;
import java.util.List;

public class AuditEntity
{
	@Id
	private String id;
	private String string;
	private int number;
	private List<String> listStrings;
	private TestSubEntity subEntity;

	public String getId()
	{
		return id;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public String getString()
	{
		return string;
	}

	public void setString(final String string)
	{
		this.string = string;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(final int number)
	{
		this.number = number;
	}

	public List<String> getListStrings()
	{
		return listStrings;
	}

	public void setListStrings(final List<String> listStrings)
	{
		this.listStrings = listStrings;
	}

	public TestSubEntity getSubEntity()
	{
		return subEntity;
	}

	public void setSubEntity(final TestSubEntity subEntity)
	{
		this.subEntity = subEntity;
	}

	public static class TestSubEntity
	{
		private String subString;
		private int subNumber;

		public String getSubString()
		{
			return subString;
		}

		public void setSubString(final String subString)
		{
			this.subString = subString;
		}

		public int getSubNumber()
		{
			return subNumber;
		}

		public void setSubNumber(final int subNumber)
		{
			this.subNumber = subNumber;
		}
	}
}
