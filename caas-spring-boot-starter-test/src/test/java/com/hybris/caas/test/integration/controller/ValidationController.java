package com.hybris.caas.test.integration.controller;

import com.hybris.caas.web.validator.Identifier;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/validations")
public class ValidationController
{
	private static final Logger LOG = LoggerFactory.getLogger(ValidationController.class);

	@GetMapping("/identifier/{id}")
	public String validateIdentifier(@PathVariable @Identifier final String id)
	{
		LOG.info("GET /identifier/" + id);
		Assert.fail("Should throw InvalidIdentifierException.");
		return "OK";
	}
}