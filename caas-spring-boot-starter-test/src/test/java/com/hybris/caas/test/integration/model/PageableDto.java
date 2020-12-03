package com.hybris.caas.test.integration.model;

import com.hybris.caas.web.pagination.PageableCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PageableDto
{
	private PageableCollection.PageMetaData page;
	private List<PageableCollection.SortMetaData> sort = new ArrayList<>();

	public PageableDto()
	{
		// empty constructor
	}

	public static PageableDto of(final Pageable pageable)
	{
		return new PageableDto(pageable);
	}

	private PageableDto(final Pageable pageable)
	{
		this.page = new PageableCollection.PageMetaData();
		this.page.setNumber(pageable.getPageNumber());
		this.page.setSize(pageable.getPageSize());

		if (Objects.nonNull(pageable.getSort()) && pageable.getSort().isSorted())
		{
			for (final Sort.Order order : pageable.getSort())
			{
				final PageableCollection.SortMetaData innerSort = new PageableCollection.SortMetaData();
				innerSort.setDirection(order.getDirection().name());
				innerSort.setProperty(order.getProperty());
				sort.add(innerSort);
			}
		}
	}

	public PageableCollection.PageMetaData getPage()
	{
		return page;
	}

	public void setPage(final PageableCollection.PageMetaData page)
	{
		this.page = page;
	}

	public List<PageableCollection.SortMetaData> getSort()
	{
		return sort;
	}

	public void setSort(final List<PageableCollection.SortMetaData> sort)
	{
		this.sort = sort;
	}
}
