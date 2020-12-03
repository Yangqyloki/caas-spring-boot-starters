package com.hybris.caas.web;

import com.google.common.collect.Sets;
import com.hybris.caas.error.exception.ParameterSizeExceededException;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.hybris.caas.web.Constants.IDENTIFIER_REG_EXP;
import static com.hybris.caas.web.ControllerUtils.getExpandAsList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ControllerUtilsTest
{
	private static final Pattern UUID_REGEX_PATTERN = Pattern.compile(IDENTIFIER_REG_EXP);
	private static final String UUID_VALID = "aaaaaaaa-8d66-47e6-9d0c-f173ef8baaaa";
	private static final String UUID_VALID_TOO = "bbbbbbbb-8d66-47e6-9d0c-f173ef8bbbbb";

	@Test
	public void shouldFilterIdentifiersWhenInvalidElements()
	{
		final List<String> listAllInvalid = new ArrayList<>(Arrays.asList("INVALID", "b", ""));
		final List<String> listOneValid = new ArrayList<>(Arrays.asList(UUID_VALID, "INVALID"));

		assertEquals(ControllerUtils.filterIdentifiers(listAllInvalid, UUID_REGEX_PATTERN), new ArrayList<>());
		assertEquals(ControllerUtils.filterIdentifiers(listOneValid, UUID_REGEX_PATTERN), new ArrayList<>(Arrays.asList(UUID_VALID)));
	}

	@Test
	public void shouldNotFilterIdentifiersWhenAllValidElements()
	{
		final List<String> emptyList = new ArrayList<>();
		final List<String> listAllValid = new ArrayList<>(Arrays.asList(UUID_VALID, UUID_VALID_TOO));
		final List<String> listElementProvidedTwice = new ArrayList<>(Arrays.asList(UUID_VALID, UUID_VALID));

		assertEquals(ControllerUtils.filterIdentifiers(null, UUID_REGEX_PATTERN), new ArrayList<>());
		assertEquals(ControllerUtils.filterIdentifiers(emptyList, UUID_REGEX_PATTERN), new ArrayList<>());
		assertEquals(ControllerUtils.filterIdentifiers(listAllValid, UUID_REGEX_PATTERN),
				new ArrayList<>(Arrays.asList(UUID_VALID, UUID_VALID_TOO)));
		assertEquals(ControllerUtils.filterIdentifiers(listElementProvidedTwice, UUID_REGEX_PATTERN),
				new ArrayList<>(Arrays.asList(UUID_VALID, UUID_VALID)));
	}

	@Test
	public void testHasFilteredValidIds()
	{
		final List<String> emptyList = new ArrayList<>();
		final List<String> listAllValid = new ArrayList<>(Arrays.asList(UUID_VALID, UUID_VALID_TOO));
		final List<String> listElementProvidedTwice = new ArrayList<>(Arrays.asList(UUID_VALID, UUID_VALID));

		assertFalse(ControllerUtils.hasFilteredValidIds(null, null));
		assertFalse(ControllerUtils.hasFilteredValidIds(null, UUID_REGEX_PATTERN));
		assertFalse(ControllerUtils.hasFilteredValidIds(emptyList, null));
		assertFalse(ControllerUtils.hasFilteredValidIds(emptyList, UUID_REGEX_PATTERN));
		assertFalse(ControllerUtils.hasFilteredValidIds(listAllValid, UUID_REGEX_PATTERN));
		assertFalse(ControllerUtils.hasFilteredValidIds(listElementProvidedTwice, UUID_REGEX_PATTERN));

		assertTrue(ControllerUtils.hasFilteredValidIds(new ArrayList<>(Arrays.asList(UUID_VALID, "abc")), UUID_REGEX_PATTERN));
	}

	@Test
	public void shouldSplitParam()
	{
		final List<String> elements = ControllerUtils.splitParameterByComma("P01, P02, P03 ,P04 ,P05,P06");
		assertThat(elements, contains("P01", " P02", " P03 ", "P04 ", "P05", "P06"));

		final List<String> emptyListOnEmptyString = ControllerUtils.splitParameterByComma("");
		assertThat(emptyListOnEmptyString, is(empty()));

		final List<String> emptyListOnNull = ControllerUtils.splitParameterByComma(null);
		assertThat(emptyListOnNull, is(empty()));

		final List<String> listWithTwoEmptyElementsOnCommaString = ControllerUtils.splitParameterByComma(",");
		assertThat(listWithTwoEmptyElementsOnCommaString, contains("", ""));

		final List<String> listWithTwoElementsOnCommaStringWithSpace = ControllerUtils.splitParameterByComma(", ");
		assertThat(listWithTwoElementsOnCommaStringWithSpace, contains("", " "));

		final List<String> listWithThreeElementsOnCommaStringWithQuotes = ControllerUtils.splitParameterByComma(",\"\",\"\" ");
		assertThat(listWithThreeElementsOnCommaStringWithQuotes, contains("", "\"\"", "\"\" "));
	}

	@Test
	public void shouldCheckParameterSize()
	{
		ControllerUtils.checkParameterSize(Arrays.asList("0", "1", "2", "3", "4"), "paramName", 5);
	}

	@Test(expected = ParameterSizeExceededException.class)
	public void shouldThrowExceptionWhenCheckParameterSizeIfParameterSizeExceedsMaxConfigured()
	{
		ControllerUtils.checkParameterSize(Arrays.asList("0", "1", "2", "3", "4", "5"), "paramName", 3);
	}

	@Test
	public void shouldSplitParameterAndCheckParameterSize()
	{
		final List<String> lst = ControllerUtils.splitParameterAndCheckParameterSize("1,2,3,4", "paramName", 5);

		assertThat(lst.size(), is(4));
	}

	@Test(expected = ParameterSizeExceededException.class)
	public void shouldThrowExceptionWhenSplitParameterAndCheckParameterSizeIfParameterSizeExceedsMaxConfigured()
	{
		ControllerUtils.splitParameterAndCheckParameterSize("1,2,3,4,5,6", "paramName", 5);
	}

	@Test
	public void shouldGetExpandAsList()
	{
		final List<String> result = getExpandAsList("editionIds,abc,paths,position", Sets.newHashSet("editionIds", "paths"),
				Sets.newHashSet("position"));
		assertThat(result, allOf(containsInAnyOrder("editionIds", "paths", "position"), not(containsInAnyOrder("abc"))));
	}

	@Test
	public void shouldGetExpandAsListDefaultExpandIsNull()
	{
		final List<String> result = getExpandAsList("editionIds,abc,paths", Sets.newHashSet("editionIds", "paths"), null);
		assertThat(result, allOf(containsInAnyOrder("editionIds", "paths"), not(containsInAnyOrder("abc"))));
	}

	@Test
	public void shouldGetEmptyListWhenExpandIsNull()
	{
		final List<String> result = getExpandAsList(null, Sets.newHashSet("editionIds", "paths"), null);
		assertThat(result, IsEmptyCollection.empty());
	}

	@Test
	public void shouldGetEmptyListWhenValidExpandIsNull()
	{
		final List<String> result = getExpandAsList("editionIds,abc,paths", null, null);
		assertThat(result, IsEmptyCollection.empty());
	}

	@Test
	public void shouldGetOnlyDefaultExpandWhenValidExpandIsNull()
	{
		final List<String> result = getExpandAsList("editionIds,abc", null, Sets.newHashSet("position"));
		assertThat(result, allOf(contains("position"), not(contains("editionIds", "abc"))));
	}

	@Test
	public void shouldGetOnlyDefaultExpandWhenParamIsEmpty()
	{
		final List<String> result = getExpandAsList("", null, Sets.newHashSet("position"));
		assertThat(result, contains("position"));
		assertThat(result.size(), is(1));
	}

	@Test
	public void shouldGetMultipleValuesWithCommasInValues()
	{
		final List<String> result = getExpandAsList("editionIds,co,mmas,com%2Cmas,commas",
													Sets.newHashSet("co,mmas", "com%2Cmas", "commas"));
		assertThat(result.size(), is(2));
		assertThat(result, containsInAnyOrder("com%2Cmas", "commas"));
	}
}
