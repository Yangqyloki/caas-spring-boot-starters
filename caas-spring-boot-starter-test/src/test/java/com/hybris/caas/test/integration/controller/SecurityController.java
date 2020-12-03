package com.hybris.caas.test.integration.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/security")
public class SecurityController
{
	private static final Logger LOG = LoggerFactory.getLogger(SecurityController.class);

	@GetMapping("/authenticated")
	public String requiresAuthentication()
	{
		LOG.info("GET /security/authenticated");
		return "OK";
	}

	@PreAuthorize("hasAuthority('read_only')")
	@GetMapping("/authorized")
	public String requiresAuthorization()
	{
		LOG.info("GET /security/authorized");
		return "OK";
	}

	@GetMapping("/public")
	public String publicEndpoint()
	{
		LOG.info("GET /security/public");
		return "OK";
	}
}