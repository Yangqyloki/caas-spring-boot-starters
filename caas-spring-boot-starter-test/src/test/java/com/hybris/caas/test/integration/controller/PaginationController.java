package com.hybris.caas.test.integration.controller;

import com.hybris.caas.test.integration.model.PageableDto;
import com.hybris.caas.web.pagination.MaxPageSize;
import com.hybris.caas.web.sort.SortProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequestMapping("/pagination")
@RestController
public class PaginationController
{
	private static final Logger LOG = LoggerFactory.getLogger(PaginationController.class);

	@GetMapping("/defaults")
	public PageableDto defaultPagingAndSorting(final Pageable pageable)
	{
		LOG.info("GET /pagination/defaults");
		return PageableDto.of(pageable);
	}

	@GetMapping("/custom-defaults")
	public PageableDto customDefaultPagingAndSorting(
			@PageableDefault(size = 100, page = 100, sort = {"foo", "bar", "baz"}, direction = Sort.Direction.DESC) final Pageable pageable)
	{
		LOG.info("GET /pagination/custom-defaults");
		return PageableDto.of(pageable);
	}

	@GetMapping("/filter-sort")
	public PageableDto filterSortAttributes(@SortProperties({"foo", "bar"}) final Pageable pageable)
	{
		LOG.info("GET /pagination/custom/filter-sort");
		return PageableDto.of(pageable);
	}

	@GetMapping("/max-page-size")
	public PageableDto maxPageSize(@MaxPageSize(100) final Pageable pageable)
	{
		LOG.info("GET /pagination/custom/max-page-size");
		return PageableDto.of(pageable);
	}
}
