package com.hybris.caas.test.integration.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/fail")
public class FailController
{
	private static final Logger LOG = LoggerFactory.getLogger(FailController.class);

	@GetMapping
	public void fail(@RequestParam(required = false) final Optional<String> location)
	{
		LOG.info("GET /fail?location=" + location.orElse("before") + ".");
	}
}