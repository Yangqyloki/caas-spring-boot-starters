package com.hybris.caas.test.integration;

import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.test.integration.model.PageableDto;
import com.hybris.caas.test.integration.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

public class WebStarterIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private TestRestTemplate restTemplate;
	private HttpHeaders headers;
	private HttpEntity<?> request;

	@BeforeEach
	public void setUp()
	{
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		request = new HttpEntity<>(headers);
	}

	@Test
	public void should_handle_InvalidIdentifierException()
	{
		final ResponseEntity<ErrorMessage> response = restTemplate.getForEntity("/validations/identifier/1", ErrorMessage.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void should_convert_guid_in_path_var_to_lowercase()
	{
		final String id = "AbCd-1234-EfD";
		final Person person = restTemplate.getForObject("/persons/" + id, Person.class);

		assertThat(person.getId()).isEqualTo(id.toLowerCase(Locale.getDefault()));
	}

	@Test
	public void should_convert_guid_in_req_param_to_lowercase()
	{
		final String id = "AbCd-1234-EfD";
		final Person person = restTemplate.getForObject("/persons?id=" + id, Person.class);

		assertThat(person.getId()).isEqualTo(id.toLowerCase(Locale.getDefault()));
	}

	@Test
	public void should_convert_guid_in_req_body_to_lowercase()
	{
		request = new HttpEntity<>(Person.BOB, headers);

		final ResponseEntity<Person> response = restTemplate.exchange("/persons", POST, request, Person.class);
		final Person person = response.getBody();

		assertThat(person.getId()).isEqualTo("usid-person-007");
	}

	@Test
	public void should_use_defaults_paging_and_sorting()
	{
		final PageableDto pageable = restTemplate.getForObject("/pagination/defaults", PageableDto.class);

		// Spring PageableHandlerMethodArgumentResolver will not reduce the number by 1 when using defaults (0 is the actual default).
		assertThat(pageable.getPage().getNumber()).isEqualTo(0);
		assertThat(pageable.getPage().getSize()).isEqualTo(20);
		assertThat(pageable.getSort()).isEmpty();
	}

	@Test
	public void should_use_custom_defaults_paging_and_sorting()
	{
		final PageableDto pageable = restTemplate.getForObject("/pagination/custom-defaults", PageableDto.class);

		assertThat(pageable.getPage().getNumber()).isEqualTo(100);
		assertThat(pageable.getPage().getSize()).isEqualTo(100);

		assertThat(pageable.getSort()).hasSize(3);
		assertThat(pageable.getSort().get(0).getProperty()).isEqualTo("foo");
		assertThat(pageable.getSort().get(0).getDirection()).isEqualTo("DESC");
		assertThat(pageable.getSort().get(1).getProperty()).isEqualTo("bar");
		assertThat(pageable.getSort().get(1).getDirection()).isEqualTo("DESC");
		assertThat(pageable.getSort().get(2).getProperty()).isEqualTo("baz");
		assertThat(pageable.getSort().get(2).getDirection()).isEqualTo("DESC");
	}

	@Test
	public void should_override_custom_defaults_paging_and_sorting()
	{
		final PageableDto pageable = restTemplate.getForObject(
				"/pagination/custom-defaults?pageNumber=10&pageSize=10&sort=abc:ASC,def:DESC", PageableDto.class);

		// Pageable is zero-indexed, so the resolver will reduce page number by 1 since the web layer is one-indexed.
		assertThat(pageable.getPage().getNumber()).isEqualTo(9);
		assertThat(pageable.getPage().getSize()).isEqualTo(10);

		assertThat(pageable.getSort()).hasSize(2);
		assertThat(pageable.getSort().get(0).getProperty()).isEqualTo("abc");
		assertThat(pageable.getSort().get(0).getDirection()).isEqualTo("ASC");
		assertThat(pageable.getSort().get(1).getProperty()).isEqualTo("def");
		assertThat(pageable.getSort().get(1).getDirection()).isEqualTo("DESC");
	}

	@Test
	public void should_use_default_maxPageSize()
	{
		final PageableDto pageable = restTemplate.getForObject("/pagination/custom-defaults?pageSize=3000", PageableDto.class);

		assertThat(pageable.getPage().getSize()).isEqualTo(1000);
	}

	@Test
	public void should_use_custom_maxPageSize()
	{
		final PageableDto pageable = restTemplate.getForObject("/pagination/max-page-size?pageSize=3000", PageableDto.class);

		assertThat(pageable.getPage().getSize()).isEqualTo(100);
	}

	@Test
	public void should_ignore_multiple_sorting_parameters()
	{
		final PageableDto pageable = restTemplate.getForObject("/pagination/custom-defaults?sort=abc:ASC&sort=def:DESC", PageableDto.class);

		assertThat(pageable.getSort()).hasSize(1);
		assertThat(pageable.getSort().get(0).getProperty()).isEqualTo("abc");
		assertThat(pageable.getSort().get(0).getDirection()).isEqualTo("ASC");
	}
}
