package com.hybris.caas.web.validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnumValuesValidatorTest
{

	private ConfigurableApplicationContext ctx;

	private LocalValidatorFactoryBean validator;

	private ValidEnumClass validEnumClass;

	@Before
	public void setUp() throws Exception
	{
		ctx = new AnnotationConfigApplicationContext(LocalValidatorFactoryBean.class);
		validator = ctx.getBean(LocalValidatorFactoryBean.class);
		validEnumClass = new ValidEnumClass();
	}

	@After
	public void tearDown() throws Exception
	{
		ctx.close();
	}

	@Test
	public void shouldPassValidationIfFieldNull() throws Exception
	{
		final BeanPropertyBindingResult result = new BeanPropertyBindingResult(validEnumClass, "validEnumClass");
		validator.validate(validEnumClass, result);

		assertThat(result.getAllErrors(), is(empty()));
	}

	@Test
	public void shouldPassEmptyFieldValidation() throws Exception
	{
		validEnumClass.setFieldOne(MyTestEnum.ONE.name());
		validEnumClass.setFieldTwo(MyTestEnum.THREE.name());

		final BeanPropertyBindingResult result = new BeanPropertyBindingResult(validEnumClass, "validEnumClass");
		validator.validate(validEnumClass, result);

		assertThat(result.getAllErrors(), is(empty()));
	}

	@Test
	public void shouldFailValidation() throws Exception
	{
		validEnumClass.setFieldOne("invalid value");
		validEnumClass.setFieldTwo(MyTestEnum.THREE.name());

		final BeanPropertyBindingResult result = new BeanPropertyBindingResult(validEnumClass, "validEnumClass");
		validator.validate(validEnumClass, result);

		assertThat(result.getErrorCount(), is(1));
		validateObjectError(result.getAllErrors().get(0));
	}

	@Test
	public void shouldFailMultipleValidation() throws Exception
	{
		validEnumClass.setFieldOne("invalid value");
		validEnumClass.setFieldTwo("anotherInvalidValue");
		validEnumClass.setFieldThree("evenOneMoreInvalidValue");

		final BeanPropertyBindingResult result = new BeanPropertyBindingResult(validEnumClass, "validEnumClass");
		validator.validate(validEnumClass, result);

		assertThat(result.getErrorCount(), is(3));
		validateObjectError(result.getAllErrors().get(0));
		validateObjectError(result.getAllErrors().get(1));
		validateObjectError(result.getAllErrors().get(2));
	}

	@Test
	public void shouldNotAddOriginalValueToErrorDisplayMessage() throws Exception
	{
		validEnumClass.setFieldOne("INVALID");

		final BeanPropertyBindingResult result = new BeanPropertyBindingResult(validEnumClass, "validEnumClass");
		validator.validate(validEnumClass, result);

		assertThat(result.getAllErrors().iterator().next().getDefaultMessage(), not(containsString("INVALID")));
	}

	private void validateObjectError(final ObjectError objectError)
	{
		assertThat(objectError.getObjectName(), equalTo("validEnumClass"));
		assertThat(Arrays.asList(objectError.getCodes()), everyItem(startsWith("EnumValues")));
	}

	/**
	 * This class initializes two validators:
	 * One for the fieldOne and fieldTwo, as they used same Enum class
	 * One for fieldThree, new one because has a different Enum class
	 */
	static class ValidEnumClass
	{

		@EnumValues(MyTestEnum.class)
		private String fieldOne;

		@EnumValues(MyTestEnum.class)
		private String fieldTwo;

		@EnumValues(AnotherTestEnum.class)
		private String fieldThree;

		public String getFieldOne()
		{
			return fieldOne;
		}

		public void setFieldOne(final String fieldOne)
		{
			this.fieldOne = fieldOne;
		}

		public String getFieldTwo()
		{
			return fieldTwo;
		}

		public void setFieldTwo(final String fieldTwo)
		{
			this.fieldTwo = fieldTwo;
		}

		public String getFieldThree()
		{
			return fieldThree;
		}

		public void setFieldThree(final String fieldThree)
		{
			this.fieldThree = fieldThree;
		}
	}

	enum MyTestEnum
	{
		ONE, TWO, THREE
	}

	enum AnotherTestEnum
	{
		UNO, DOS, TRES
	}

}

