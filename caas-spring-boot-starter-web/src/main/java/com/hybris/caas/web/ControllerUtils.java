package com.hybris.caas.web;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.hybris.caas.error.exception.ParameterSizeExceededException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * This is a Helper class for web controller.
 */
public final class ControllerUtils
{
	private ControllerUtils()
	{
		// Empty constructor.
	}

	/**
	 * Removes identifiers from a list that are not matching the regular expression provided.
	 *
	 * @param identifiers  the list of element to apply the filter.
	 * @param regexPattern the regular expression to match.
	 * @return the list of valid element.
	 */
	public static List<String> filterIdentifiers(final List<String> identifiers, final Pattern regexPattern)
	{
		return Optional.ofNullable(identifiers)
				.map(item -> item.stream().filter(id -> regexPattern.matcher(id).matches()).collect(toList()))
				.orElse(Collections.emptyList());
	}

	/**
	 * This method removes elements from the list that are not valid identifiers by matching the identifier regular expression.
	 * Returns {@code true} if elements were filtered/removed from {@code idList},
	 * otherwise false
	 *
	 * @param idList       the list of identifiers to filter
	 * @param regexPattern the regular expression to match.
	 * @return {@code true} if elements were filtered/removed from {@code idList}, otherwise false
	 */
	public static boolean hasFilteredValidIds(final List<String> idList, final Pattern regexPattern)
	{
		if (Objects.isNull(idList) || Objects.isNull(regexPattern))
		{
			return false;
		}

		final int originalSize = idList.size();
		final List<String> validIdList = idList.stream().filter(id -> regexPattern.matcher(id).matches()).collect(toList());
		idList.clear();
		idList.addAll(validIdList);
		return originalSize != validIdList.size();
	}

	/**
	 * This method generates a list of parameter values split by comma.
	 *
	 * @param paramValue the initial value of the parameter.
	 * @return List of elements split by comma or empty list
	 */
	public static List<String> splitParameterByComma(final String paramValue)
	{
		return Lists.newArrayList(Optional.ofNullable(paramValue)
				.map(p -> Arrays.asList(StringUtils.commaDelimitedListToStringArray(p)))
				.orElse(Collections.emptyList()));
	}

	/**
	 * This method verifies if the size of {@code paramValues} does not exceed the {@code maxSizeConfigured}.
	 *
	 * @param paramValues       the list of query parameter values.
	 * @param paramName         the name of the query parameter validated.
	 * @param maxSizeConfigured the maximum size supported.
	 * @throws ParameterSizeExceededException if {@code paramValues} size exceeds the {@code maxSizeConfigured}
	 */
	public static void checkParameterSize(final List<String> paramValues, final String paramName, final int maxSizeConfigured)
	{
		if (paramValues.size() > maxSizeConfigured)
		{
			throw new ParameterSizeExceededException(paramName, maxSizeConfigured, paramValues.size());
		}
	}

	/**
	 * This method combines the split of the generation of the list of parameter values split by comma and the verification
	 * if the size of {@code paramValues} does not exceed the {@code maxSizeConfigured}.
	 *
	 * @param paramValue   the initial value of the parameter.
	 * @param paramName    the name of the query parameter validated.
	 * @param maxParamSize the maximum size supported.
	 * @return List of elements split by comma or empty list
	 */
	public static List<String> splitParameterAndCheckParameterSize(final String paramValue, final String paramName,
			final int maxParamSize)
	{
		final List<String> parameters = splitParameterByComma(paramValue);
		checkParameterSize(parameters, paramName, maxParamSize);
		return parameters;
	}

	/**
	 * Converts the expand {@code paramValue} to a {@link List}
	 * It filters out fields that are not supported.
	 *
	 * @param paramValue   the value of the parameter
	 * @param validExpands a {@link List} of valid fields
	 * @return List of expands parameters
	 */
	public static List<String> getExpandAsList(final String paramValue, final Set<String> validExpands)
	{
		return getExpandAsList(paramValue, validExpands, ImmutableSet.of());
	}

	/**
	 * Converts the expand {@code paramValue} to a {@link List}
	 * It filters out fields that are not supported.
	 * It also adds the default expands to the returning list.
	 *
	 * @param paramValue     the value of the parameter
	 * @param validExpands   a {@link List} of valid fields
	 * @param defaultExpands a set of default expands
	 * @return List of expands parameters
	 */
	public static List<String> getExpandAsList(final String paramValue, final Set<String> validExpands, final Set<String> defaultExpands)
	{
		final Set<String> result = ControllerUtils.splitParameterByComma(paramValue)
				.stream()
				.filter(v -> CollectionUtils.isNotEmpty(validExpands) && validExpands.contains(v))
				.collect(Collectors.toSet());
		if (CollectionUtils.isNotEmpty(defaultExpands))
		{
			result.addAll(defaultExpands);
		}

		return new ArrayList<>(result);
	}
}
