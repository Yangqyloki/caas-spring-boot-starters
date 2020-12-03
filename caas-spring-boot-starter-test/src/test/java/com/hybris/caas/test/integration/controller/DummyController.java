package com.hybris.caas.test.integration.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(path = "/dummy", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class DummyController
{
	private static final Logger LOG = LoggerFactory.getLogger(DummyController.class);

	@GetMapping()
	public String dummy()
	{
		LOG.info("GET /dummy");
		return "OK";
	}
}