package com.hybris.caas.test.integration.controller;

import com.hybris.caas.test.integration.model.Person;
import com.hybris.caas.web.annotation.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RequestMapping("/persons")
@RestController
public class PersonController
{
	private static final Logger LOG = LoggerFactory.getLogger(PersonController.class);

	@PostMapping
	public Person validatePerson(@RequestBody @Valid final Person person)
	{
		LOG.info("POST /persons");
		return person;
	}

	@GetMapping()
	public Person get(@RequestParam @Guid final String id)
	{
		LOG.info("GET /persons?id=" + id);
		return new Person("nobody", id);
	}

	@GetMapping("/{id}")
	public Person getByName(@PathVariable @Guid final String id)
	{
		LOG.info("GET /persons/" + id);
		return new Person("nobody", id);
	}

}
