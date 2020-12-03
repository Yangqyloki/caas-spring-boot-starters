package com.hybris.caas.web.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Component
@ConditionalOnProperty(prefix = "server", name = "use-forwarded-header-filter", matchIfMissing = true)
public class CaasForwardedHeaderFilter extends ForwardedHeaderFilter implements OrderedFilter
{

	public static final int FORWARDED_HEADER_ORDER = REQUEST_WRAPPER_FILTER_MAX_ORDER - 50;

	@Override
	public int getOrder()
	{
		return FORWARDED_HEADER_ORDER;
	}

}
