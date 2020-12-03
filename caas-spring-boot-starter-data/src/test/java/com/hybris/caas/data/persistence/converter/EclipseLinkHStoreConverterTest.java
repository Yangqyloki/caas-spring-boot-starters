package com.hybris.caas.data.persistence.converter;

import com.hybris.caas.data.persistence.converter.eclipselink.EclipseLinkHStoreConverter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EclipseLinkHStoreConverterTest
{

	EclipseLinkHStoreConverter converter = new EclipseLinkHStoreConverter();

	@Test
	public void should_convert_null_database_value()
	{
		assertEquals(0, converter.convertToEntityAttribute(null).size());
	}

}
