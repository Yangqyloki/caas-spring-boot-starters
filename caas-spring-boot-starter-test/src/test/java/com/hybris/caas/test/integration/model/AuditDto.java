package com.hybris.caas.test.integration.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class AuditDto
{

	@NotEmpty
	private String string;

	@Min(0)
	private int number;

	private List<String> listStrings;
	private SubResource subResource;

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

	public SubResource getSubResource()
	{
		return subResource;
	}

	public void setSubResource(final SubResource subResource)
	{
		this.subResource = subResource;
	}

	public static class SubResource
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
