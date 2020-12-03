package com.hybris.caas.log.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.hybris.caas.log.util.JsonNodeUtils.AttributeDiff;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.hybris.caas.log.util.JsonNodeUtils.diff;
import static com.hybris.caas.log.util.JsonNodeUtils.getKeys;
import static com.hybris.caas.log.util.JsonNodeUtils.valueAsText;
import static com.hybris.caas.log.util.JsonNodeUtils.valueToTree;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonNodeUtilsTest
{
	private static final String ID = "id";
	private static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	private static final List<String> BOBS_PHONES = Arrays.asList("phone-2", "phone-3");
	private static final Address BOBS_ADDRESS = new Address();

	static
	{
		BOBS_ADDRESS.setStreetNumber(99);
		BOBS_ADDRESS.setStreetLine1("new-line-1");
		BOBS_ADDRESS.setStreetLine2("line-2");
		BOBS_ADDRESS.setCity("city");
	}

	private Person alice;
	private Person bob;

	private static final List<String> BOB_ATTR_LIST = Arrays.asList("id", "name", "phone[]", "address.streetNumber", "address.streetLine1",
			"address.streetLine2", "address.city");
	private static final List<String> BOB_ADDRESS_ATTR_LIST = Arrays.asList("streetNumber", "streetLine1", "streetLine2", "city");

	@Before
	public void setUp()
	{
		final Address aliceAddress = new Address();
		aliceAddress.setStreetNumber(99);
		aliceAddress.setStreetLine1("line-1");
		aliceAddress.setCity("city");

		alice = new Person();
		alice.setId("id");
		alice.setName("Alice");
		alice.setPhone(Arrays.asList("phone-1", "phone-2"));
		alice.setAddress(aliceAddress);

		bob = new Person();
		bob.setId("id");
		bob.setName("Bob");
		bob.setPhone(BOBS_PHONES);
		bob.setAddress(BOBS_ADDRESS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_not_apply_diff_simple_list()
	{
		diff(MAPPER.valueToTree(Arrays.asList("foo", "bar")), MAPPER.valueToTree(alice));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_not_apply_for_simple_types()
	{
		diff(MAPPER.valueToTree(alice), MAPPER.valueToTree("bar"));
	}

	@Test
	public void should_apply_diff_for_update()
	{
		final Set<AttributeDiff> diffs = diff(MAPPER.valueToTree(alice), MAPPER.valueToTree(bob));

		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo("Alice"));
		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo("Bob"));
		assertThat(diffs.stream().filter(diff -> "address.streetLine1".equals(diff.getKey())).findFirst().get().getOldValue(),
				equalTo("line-1"));
		assertThat(diffs.stream().filter(diff -> "address.streetLine1".equals(diff.getKey())).findFirst().get().getNewValue(),
				equalTo("new-line-1"));
		assertThat(diffs.stream().filter(diff -> "address.streetLine2".equals(diff.getKey())).findFirst().get().getOldValue(),
				equalTo(""));
		assertThat(diffs.stream().filter(diff -> "address.streetLine2".equals(diff.getKey())).findFirst().get().getNewValue(),
				equalTo("line-2"));
		assertThat(diffs.stream().filter(diff -> "phone.0".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo("phone-1"));
		assertThat(diffs.stream().filter(diff -> "phone.0".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.1".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.1".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo("phone-3"));
	}

	@Test
	public void should_apply_diff_for_update_with_nulls()
	{
		final List<String> phonesWithNullEntry = new ArrayList<>(BOBS_PHONES);
		phonesWithNullEntry.add(null);
		phonesWithNullEntry.add("");

		alice.setName(null);
		bob.setId(null);
		bob.setPhone(phonesWithNullEntry);
		bob.getAddress().setCity(null);

		final Set<AttributeDiff> diffs = diff(MAPPER.valueToTree(alice), MAPPER.valueToTree(bob));

		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo("Bob"));
		assertThat(diffs.stream().filter(diff -> "id".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo("id"));
		assertThat(diffs.stream().filter(diff -> "id".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "address.city".equals(diff.getKey())).findFirst().get().getOldValue(),
				equalTo("city"));
		assertThat(diffs.stream().filter(diff -> "address.city".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.0".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo("phone-1"));
		assertThat(diffs.stream().filter(diff -> "phone.0".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.1".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.1".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo("phone-3"));
		assertThat(diffs.stream().filter(diff -> "phone.2".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.2".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.3".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone.3".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
	}

	@Test
	public void should_apply_diff_for_create() throws JsonProcessingException
	{
		final Set<AttributeDiff> diffs = diff(MAPPER.valueToTree(new Object()), MAPPER.valueToTree(bob));

		assertThat(diffs.stream().filter(diff -> "id".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "id".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo("id"));
		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo("Bob"));
		assertThat(diffs.stream().filter(diff -> "phone".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone".equals(diff.getKey())).findFirst().get().getNewValue(),
				equalTo(MAPPER.writeValueAsString(BOBS_PHONES)));
		assertThat(diffs.stream().filter(diff -> "address".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "address".equals(diff.getKey())).findFirst().get().getNewValue(),
				equalTo(MAPPER.writeValueAsString(BOBS_ADDRESS)));
	}

	@Test
	public void should_apply_diff_for_delete() throws JsonProcessingException
	{
		final Set<AttributeDiff> diffs = diff(MAPPER.valueToTree(bob), MAPPER.valueToTree(new Object()));

		assertThat(diffs.stream().filter(diff -> "id".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo("id"));
		assertThat(diffs.stream().filter(diff -> "id".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getOldValue(), equalTo("Bob"));
		assertThat(diffs.stream().filter(diff -> "name".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "phone".equals(diff.getKey())).findFirst().get().getOldValue(),
				equalTo(MAPPER.writeValueAsString(BOBS_PHONES)));
		assertThat(diffs.stream().filter(diff -> "phone".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
		assertThat(diffs.stream().filter(diff -> "address".equals(diff.getKey())).findFirst().get().getOldValue(),
				equalTo(MAPPER.writeValueAsString(BOBS_ADDRESS)));
		assertThat(diffs.stream().filter(diff -> "address".equals(diff.getKey())).findFirst().get().getNewValue(), equalTo(""));
	}

	@Test
	public void should_convert_value_as_text() throws JsonProcessingException
	{
		final Person one = new Person();
		one.setId("1");
		final List<String> two = Arrays.asList("1", "2");

		assertThat(valueAsText(MissingNode.getInstance()), equalTo(""));
		assertThat(valueAsText(NullNode.getInstance()), equalTo(""));
		assertThat(valueAsText(new POJONode("foo")), equalTo("foo"));
		assertThat(valueAsText(MAPPER.valueToTree(one)), equalTo(MAPPER.writeValueAsString(one)));
		assertThat(valueAsText(MAPPER.valueToTree(two)), equalTo(MAPPER.writeValueAsString(two)));
	}

	@Test
	public void should_convert_value_to_tree()
	{
		assertThat(valueToTree(alice), equalTo(MAPPER.valueToTree(alice)));
	}

	@Test
	public void should_get_keys_from_nested_object()
	{
		final List<String> attrList = getKeys(valueToTree(bob));
		assertThat(attrList, equalTo(BOB_ATTR_LIST));
	}

	@Test
	public void should_get_keys_from_flat_object()
	{
		final List<String> attrList = getKeys(valueToTree(BOBS_ADDRESS));
		assertThat(attrList, equalTo(BOB_ADDRESS_ATTR_LIST));
	}

	@Test
	public void should_not_return_keys_for_no_attribute_object()
	{
		final List<String> attrList = getKeys(valueToTree(new Object()));
		assertThat(attrList, equalTo(Collections.emptyList()));
	}

	@Test
	public void should_handle_passing_value_node_as_root_during_get_keys()
	{
		final List<String> attrList = getKeys(valueToTree(bob.getId()));
		assertThat(attrList, equalTo(Arrays.asList(ID)));
	}

	private static class Person
	{
		private String id;
		private String name;
		private List<String> phone;
		private Address address;

		public String getId()
		{
			return id;
		}

		public void setId(final String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(final String name)
		{
			this.name = name;
		}

		public List<String> getPhone()
		{
			return phone;
		}

		public void setPhone(final List<String> phone)
		{
			this.phone = phone;
		}

		public Address getAddress()
		{
			return address;
		}

		public void setAddress(final Address address)
		{
			this.address = address;
		}
	}

	private static class Address
	{
		private int streetNumber;
		private String streetLine1;
		private String streetLine2;
		private String city;

		public int getStreetNumber()
		{
			return streetNumber;
		}

		public void setStreetNumber(final int streetNumber)
		{
			this.streetNumber = streetNumber;
		}

		public String getStreetLine1()
		{
			return streetLine1;
		}

		public void setStreetLine1(final String streetLine1)
		{
			this.streetLine1 = streetLine1;
		}

		public String getStreetLine2()
		{
			return streetLine2;
		}

		public void setStreetLine2(final String streetLine2)
		{
			this.streetLine2 = streetLine2;
		}

		public String getCity()
		{
			return city;
		}

		public void setCity(final String city)
		{
			this.city = city;
		}
	}
}
