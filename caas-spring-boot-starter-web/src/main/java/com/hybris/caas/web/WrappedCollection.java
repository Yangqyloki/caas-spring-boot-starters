package com.hybris.caas.web;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Collection wrapper to be used by HTTP request/response DTOs to ensure added extensibility instead of using JSON arrays directly.
 * @param <T> the type of the collection to wrap
 */
public class WrappedCollection<T>
{
	@Valid
	@JsonProperty("value")
	private List<T> value;

	public List<T> getValue()
	{
		return value;
	}

	public void setValue(final List<T> value)
	{
		this.value = value;
	}

	public WrappedCollection()
	{
		this.value = new ArrayList();
	}

	public WrappedCollection(final List<T> value)
	{
		this.value = value;
	}

	/**
	 * Null-safe convenience method to stream the elements in the wrapped collection.
	 * @return stream of elements in the wrapped collection
	 */
	public Stream<T> stream()
	{
		return Optional.ofNullable(value).map(List::stream).orElse(Stream.empty());
	}

	/**
	 * Static factory method for creating the wrapped collection.
	 *
	 * @param elements the elements to wrap
	 * @param <T> the type of the collection
	 * @return the wrapped collection
	 */
	public static <T> WrappedCollection<T> of(final List<T> elements)
	{
		return new WrappedCollection<>(elements);
	}

	/**
	 * Static factory method for creating the wrapped collection.
	 *
	 * @param elements the elements to wrap
	 * @param <T> the type of the collection
	 * @return the wrapped collection
	 */
	public static <T> WrappedCollection<T> of(T... elements)
	{
		return new WrappedCollection<>(Arrays.asList(elements));
	}
}
