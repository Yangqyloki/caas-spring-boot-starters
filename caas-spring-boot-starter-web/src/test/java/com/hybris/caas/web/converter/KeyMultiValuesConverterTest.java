package com.hybris.caas.web.converter;

import com.hybris.caas.web.annotation.KeyMultiValues;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyMultiValuesConverterTest
{
	private KeyMultiValuesConverter keyMultiValuesConverter;

	@Mock
	private TypeDescriptor sourceType;

	@Mock
	private TypeDescriptor targetType;

	@Mock
	private KeyMultiValues keyMultiValues;

	@Before
	public void setup()
	{
		keyMultiValuesConverter = Mockito.spy(new KeyMultiValuesConverter());
	}

	@Test
	public void should_fail_when_annotation_not_present()
	{
		when(targetType.getAnnotation(KeyMultiValues.class)).thenReturn(null);

		assertThat(keyMultiValuesConverter.matches(sourceType, targetType), is(false));
	}

	@Test
	public void should_succeed_when_annotation_is_present()
	{
		when(targetType.getAnnotation(KeyMultiValues.class)).thenReturn(keyMultiValues);
		final Object object = keyMultiValuesConverter.convert("value1,value2", sourceType, targetType);
		final List<String> list = (List<String>)object;

		assertThat(keyMultiValuesConverter.matches(sourceType, targetType), is(true));
		assertThat(list.size(), is(1));
		assertThat(list, containsInAnyOrder("value1,value2"));
	}
}
