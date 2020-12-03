package com.hybris.caas.swagger.web;

import com.hybris.caas.swagger.config.SwaggerWebConfig;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Objects;

@Controller
public class SwaggerRedirectController
{

	private Environment environment;

	public SwaggerRedirectController(final Environment environment)
	{
		this.environment = environment;
	}

	@RequestMapping("/")
	public String redirect(final HttpServletRequest request)
	{
		final String pathPrefix = environment.getProperty("caas.swagger.server.context");

		// the {@link ForwardedHeaderFilter} will take care of handling the X-Forwarded headers if present
		final URI redirectUri = Objects.isNull(pathPrefix) ?
				ServletUriComponentsBuilder.fromServletMapping(request)
						.pathSegment(SwaggerWebConfig.API_PATH_SEGMENT, "index.html")
						.build()
						.toUri() :
				ServletUriComponentsBuilder.fromRequestUri(request)
						.pathSegment(pathPrefix, SwaggerWebConfig.API_PATH_SEGMENT, "index.html")
						.build()
						.toUri();

		return "redirect:" + redirectUri;
	}
}
