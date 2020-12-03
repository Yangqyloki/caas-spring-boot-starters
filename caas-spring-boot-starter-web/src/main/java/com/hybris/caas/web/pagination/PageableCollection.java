package com.hybris.caas.web.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Collection wrapper to be used by HTTP response DTOs to wrap the collection and enhance the response with paging and sorting meta-data.
 * @param <T> the type of the collection to wrap
 */
public class PageableCollection<T>
{
	@JsonProperty("value")
	private List<T> value;
	@JsonProperty("page")
	private PageMetaData page;
	@JsonProperty("sort")
	private List<SortMetaData> sort;

	public PageableCollection()
	{
		this.value = new ArrayList();
	}

	public List<T> getValue()
	{
		return value;
	}

	public void setValue(final List<T> value)
	{
		this.value = value;
	}

	public PageMetaData getPage()
	{
		return page;
	}

	public void setPage(final PageMetaData page)
	{
		this.page = page;
	}

	public List<SortMetaData> getSort()
	{
		return sort;
	}

	public void setSort(final List<SortMetaData> sort)
	{
		this.sort = sort;
	}

	/**
	 * Create a new {@code PageableCollection} from a spring {@link Page} element.
	 *
	 * @param page the page of results
	 * @param <T> the type of the page contents
	 * @return the pageable collection wrapper
	 */
	public static <T> PageableCollection<T> of(final Page<T> page)
	{
		final PageableCollection pagedCollection = new PageableCollection();
		pagedCollection.setValue(page.getContent());
		pagedCollection.setPage(PageMetaData.of(page));

		Optional.ofNullable(page.getSort())
				.filter(Sort::isSorted)
				.ifPresent(sort ->
						StreamSupport.stream(Spliterators.spliteratorUnknownSize(sort.iterator(), 0), false)
								.map(SortMetaData::of)
								.collect(Collectors.toList())
				);

		return pagedCollection;
	}

	/**
	 * Container for pagination meta-data.
	 */
	public static class PageMetaData
	{
		private int number;
		private int size;
		private Long totalElements;
		private Integer totalPages;

		public int getNumber()
		{
			return number;
		}

		public void setNumber(final int number)
		{
			this.number = number;
		}

		public int getSize()
		{
			return size;
		}

		public void setSize(final int size)
		{
			this.size = size;
		}

		public Long getTotalElements()
		{
			return totalElements;
		}

		public void setTotalElements(final Long totalElements)
		{
			this.totalElements = totalElements;
		}

		public Integer getTotalPages()
		{
			return totalPages;
		}

		public void setTotalPages(final Integer totalPages)
		{
			this.totalPages = totalPages;
		}

		public static PageMetaData of(final Page<?> page)
		{
			final PageMetaData pageMetaData = new PageMetaData();
			pageMetaData.setNumber(page.getNumber() + 1);
			pageMetaData.setSize(page.getSize());
			pageMetaData.setTotalElements(page.getTotalElements());
			pageMetaData.setTotalPages(page.getTotalPages());
			return pageMetaData;
		}
	}

	/**
	 * Container for sorting meta-data.
	 */

	public static class SortMetaData
	{
		private String property;
		private String direction;

		public String getProperty()
		{
			return property;
		}

		public void setProperty(final String property)
		{
			this.property = property;
		}

		public String getDirection()
		{
			return direction;
		}

		public void setDirection(final String direction)
		{
			this.direction = direction;
		}

		public static SortMetaData of(final Sort.Order sortOrder)
		{
			final SortMetaData sortMetaData = new SortMetaData();
			sortMetaData.setProperty(sortOrder.getProperty());
			sortMetaData.setDirection(sortOrder.getDirection().name());
			return sortMetaData;
		}
	}

}