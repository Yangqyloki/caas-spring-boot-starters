package com.hybris.caas.web.sort;

import com.hybris.caas.web.sort.util.CommaSeparatedParameterWebRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Spring MVC argument resolver permitting to inject a spring data {@link Sort} param
 * into a controller. The controller parameter must be of type {@link Sort}.
 * This implementation will delegate the resolving to its super class, then remove any sort properties that are not whitelisted
 * in the {@link SortProperties} annotation if such an annotation is present.
 * The expected format of the sort string is a comma-delimited pair of attribute-sort directions separated by a colon.
 * {@code sort=attribute1:asc,attribute2:desc}
 */
public class CaasSortHandlerMethodArgumentResolver extends SortHandlerMethodArgumentResolver
{

	@Override
	public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest nativeWebRequest,
			WebDataBinderFactory binderFactory)
	{
		Sort sort = super.resolveArgument(parameter, mavContainer, new CommaSeparatedParameterWebRequest(nativeWebRequest),
				binderFactory);

		// If the SortAttributes annotation is present, then filter out invalid sort attributes.
		final SortProperties sortAttributes = parameter.getParameterAnnotation(SortProperties.class);
		if (Objects.nonNull(sortAttributes) && Objects.nonNull(sort) && sort.isSorted())
		{
			final Map<String, String> validAttributes = buildValidAttributeMap(sortAttributes);

			final List<Order> orders = new ArrayList<>();
			for (final Order order : sort)
			{
				if (validAttributes.containsKey(order.getProperty()))
				{
					orders.add(Order.by(validAttributes.get(order.getProperty())).with(order.getDirection()));
				}
			}
			sort = Sort.by(orders);
		}
		return sort;
	}

	/**
	 * Returns a map of valid property names to be used in sort parameter as keys and their column name if provided as values.
	 * If column is not provided, the value will be the same as the key.
	 */
	private Map<String, String> buildValidAttributeMap(final SortProperties sortAttributes)
	{
		final Map<String, String> attributes = new HashMap<>();
		for (int cpt = 0; cpt < sortAttributes.value().length; cpt++)
		{
			if (!Objects.isNull(sortAttributes.column()) && sortAttributes.column().length > cpt)
			{
				attributes.put(sortAttributes.value()[cpt], sortAttributes.column()[cpt]);
			}
			else
			{
				attributes.put(sortAttributes.value()[cpt], sortAttributes.value()[cpt]);
			}
		}
		return attributes;
	}
}