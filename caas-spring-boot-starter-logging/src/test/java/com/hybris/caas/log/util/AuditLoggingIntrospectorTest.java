package com.hybris.caas.log.util;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.hybris.caas.log.audit.annotation.MaskedAuditField;
import com.hybris.caas.log.audit.annotation.NonAuditableField;
import com.hybris.caas.log.audit.annotation.PartiallyMaskedAuditField;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class AuditLoggingIntrospectorTest
{
	private static final String FAKE_CREDIT_CARD = "3894238489021394";
	private static final String BOB = "Bob";
	private static final String SECRET = "DAS9823JHDSAFA";
	private static final String ALTERNATE_SECRET = "98SDAHJKJFBNVPI";
	private static final String CONTROL = "the-control-data";

	private static final ObjectMapper MAPPER = new ObjectMapper().setAnnotationIntrospectors(new AuditLoggingIntrospector(),
			new JacksonAnnotationIntrospector());

	private Person bob;
	private ExternalFakeApp fakeApp;

	@Test
	public void should_ignore_NonAuditableField_String() throws JsonProcessingException
	{
		fakeApp = new ExternalFakeApp();
		fakeApp.setControl(CONTROL);
		fakeApp.setAlternateSecret(ALTERNATE_SECRET);
		String json = MAPPER.writeValueAsString(fakeApp);

		assertThat(json, equalTo("{\"control\":\"the-control-data\",\"secret\":null}"));
	}

	@Test
	public void should_ignore_NonAuditableField_Object() throws JsonProcessingException
	{
		final Address address = new Address();
		address.setLine1("address-line-1");
		address.setCity("address-city");

		bob = new Person();
		bob.setName(BOB);
		bob.setAddress(address);
		String json = MAPPER.writeValueAsString(bob);

		assertThat(json, equalTo("{\"name\":\"Bob\",\"creditCard\":null,\"apps\":null}"));
	}

	@Test
	public void should_fully_mask_MaskedAuditField() throws JsonProcessingException
	{
		fakeApp = new ExternalFakeApp();
		fakeApp.setControl(CONTROL);
		fakeApp.setSecret(SECRET);
		String json = MAPPER.writeValueAsString(fakeApp);

		assertThat(json, equalTo("{\"control\":\"the-control-data\",\"secret\":\"**************\"}"));
	}

	@Test
	public void should_partially_mask_PartiallyMaskedAuditField() throws JsonProcessingException
	{
		bob = new Person();
		bob.setName(BOB);
		bob.setCreditCard(FAKE_CREDIT_CARD);
		String json = MAPPER.writeValueAsString(bob);

		assertThat(json, equalTo("{\"name\":\"Bob\",\"creditCard\":\"************1394\",\"apps\":null}"));
	}

	@Test
	public void should_partially_mask_PartiallyMaskedAuditField_when_value_is_4chars() throws JsonProcessingException
	{
		bob = new Person();
		bob.setName(BOB);
		bob.setCreditCard("1234");
		String json = MAPPER.writeValueAsString(bob);

		assertThat(json, equalTo("{\"name\":\"Bob\",\"creditCard\":\"1234\",\"apps\":null}"));
	}

	@Test
	public void should_partially_mask_PartiallyMaskedAuditField_when_value_smallerthan_4Chars() throws JsonProcessingException
	{
		bob = new Person();
		bob.setName(BOB);
		bob.setCreditCard("12");
		String json = MAPPER.writeValueAsString(bob);

		assertThat(json, equalTo("{\"name\":\"Bob\",\"creditCard\":\"**\",\"apps\":null}"));
	}

	@Test
	public void should_support_JsonBackReference_and_JsonManagedReference() throws JsonProcessingException
	{
		fakeApp = new ExternalFakeApp();
		fakeApp.setControl(CONTROL);
		fakeApp.setSecret(SECRET);

		bob = new Person();
		bob.setName(BOB);
		bob.setCreditCard(FAKE_CREDIT_CARD);
		bob.setApps(Collections.singletonList(fakeApp));

		String json = MAPPER.writeValueAsString(bob);

		assertThat(json, equalTo("{\"name\":\"Bob\",\"creditCard\":\"************1394\",\"apps\":[{\"control\":\"the-control-data\",\"secret\":\"**************\"}]}"));
	}

	@Test
	public void should_process_annotations_in_JsonNode() throws JsonProcessingException
	{
		final Address address = new Address();
		address.setLine1("address-line-1");
		address.setCity("address-city");

		bob = new Person();
		bob.setName(BOB);
		bob.setCreditCard(FAKE_CREDIT_CARD);
		bob.setAddress(address);

		JsonNode json = MAPPER.valueToTree(bob);

		assertThat(json.get("address"), nullValue());
		assertThat(json.get("creditCard").asText(), equalTo("************1394"));
	}

	public static class ExternalFakeApp
	{
		@JsonBackReference("apps")
		private Person owner;

		private String control;

		@MaskedAuditField
		private String secret;

		@NonAuditableField
		private String alternateSecret;

		public Person getOwner()
		{
			return owner;
		}

		public void setOwner(final Person owner)
		{
			this.owner = owner;
		}

		public String getControl()
		{
			return control;
		}

		public void setControl(final String control)
		{
			this.control = control;
		}

		public String getSecret()
		{
			return secret;
		}

		public void setSecret(final String secret)
		{
			this.secret = secret;
		}

		public String getAlternateSecret()
		{
			return alternateSecret;
		}

		public void setAlternateSecret(final String alternateSecret)
		{
			this.alternateSecret = alternateSecret;
		}
	}

	public static class Person
	{
		private String name;

		@NonAuditableField
		private Address address;

		@PartiallyMaskedAuditField
		private String creditCard;

		@JsonManagedReference("apps")
		private List<ExternalFakeApp> apps;

		public String getName()
		{
			return name;
		}

		public void setName(final String name)
		{
			this.name = name;
		}

		public Address getAddress()
		{
			return address;
		}

		public void setAddress(final Address address)
		{
			this.address = address;
		}

		public String getCreditCard()
		{
			return creditCard;
		}

		public void setCreditCard(final String creditCard)
		{
			this.creditCard = creditCard;
		}

		public List<ExternalFakeApp> getApps()
		{
			return apps;
		}

		public void setApps(final List<ExternalFakeApp> apps)
		{
			this.apps = apps;
		}
	}

	public static class Address
	{
		private String line1;
		private String line2;
		private String city;
		private String country;

		public String getLine1()
		{
			return line1;
		}

		public void setLine1(final String line1)
		{
			this.line1 = line1;
		}

		public String getLine2()
		{
			return line2;
		}

		public void setLine2(final String line2)
		{
			this.line2 = line2;
		}

		public String getCity()
		{
			return city;
		}

		public void setCity(final String city)
		{
			this.city = city;
		}

		public String getCountry()
		{
			return country;
		}

		public void setCountry(final String country)
		{
			this.country = country;
		}
	}
}