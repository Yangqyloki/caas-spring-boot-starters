package com.hybris.caas.log.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to perform a diff between two objects of the same type.
 */
public final class JsonNodeUtils
{
	private static final String JSON_NODE_LIST_ATTRIBUTE_SUFFIX = "[]";

	private static final Logger LOG = LoggerFactory.getLogger(JsonNodeUtils.class);

	// A pair of introspectors are passed under the Chain or Responsibility design pattern.
	// If the first (AuditLogging) introspector can't handle a specific annotation, the second one is called.
	private static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.setAnnotationIntrospectors(new AuditLoggingIntrospector(), new JacksonAnnotationIntrospector());

	private JsonNodeUtils()
	{
		// private constructor
	}

	/**
	 * Perform the diff between two JSON node. JSON nodes must be of type OBJECT.
	 *
	 * @param arg0 the first node
	 * @param arg1 the second node
	 * @return a set of changes between the two nodes
	 * @throws IllegalArgumentException when input nodes are not of type OBJECT
	 */
	public static Set<AttributeDiff> diff(final JsonNode arg0, final JsonNode arg1)
	{
		validateNodesForDiff(arg0, arg1);

		final EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy();
		final JsonNode patch = JsonDiff.asJson(arg0, arg1, flags);
		if (Objects.isNull(patch))
		{
			return Collections.emptySet();
		}

		final Iterator<JsonNode> nodeIterator = patch.elements();
		final Set<AttributeDiff> diffs = new HashSet<>();
		while (nodeIterator.hasNext())
		{
			final JsonNode node = nodeIterator.next();
			final String path = node.get("path").asText();
			final String key = path.substring(1).replace('/', '.');
			final String operation = node.get("op").asText();

			final JsonNode oldNode = arg0.at(path);
			final JsonNode newNode = node.get("value");

			final String oldValue;
			final String newValue;
			switch (operation)
			{
			case "replace":
				oldValue = valueAsText(oldNode);
				newValue = valueAsText(newNode);
				break;
			case "add":
				oldValue = "";
				newValue = valueAsText(newNode);
				break;
			case "remove":
				oldValue = valueAsText(oldNode);
				newValue = "";
				break;
			default:
				LOG.warn("Unknown operation: {}. The changes for {} key have not been audited", operation, key);
				continue;
			}
			diffs.add(AttributeDiff.of(key, oldValue, newValue));
		}

		return diffs;
	}

	private static void validateNodesForDiff(final JsonNode arg0, final JsonNode arg1)
	{
		if (Objects.isNull(arg0) || !arg0.isObject() || Objects.isNull(arg1) || !arg1.isObject())
		{
			throw new IllegalArgumentException("Diff can only be performed on objects");
		}
	}

	/**
	 * Object to encapsulate object attribute changes.
	 */
	public static final class AttributeDiff
	{
		private final String key;
		private final String oldValue;
		private final String newValue;

		private AttributeDiff(final String key, final String oldValue, final String newValue)
		{
			this.key = key;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public String getKey()
		{
			return key;
		}

		public String getOldValue()
		{
			return oldValue;
		}

		public String getNewValue()
		{
			return newValue;
		}

		static AttributeDiff of(final String key, final String oldValue, final String newValue)
		{
			return new AttributeDiff(key, oldValue, newValue);
		}
	}

	static String valueAsText(final JsonNode node)
	{
		if (node.isNull() || node.isMissingNode())
		{
			return "";
		}

		if (node.isValueNode())
		{
			return node.asText();
		}

		return writeValueAsString(node);
	}

	/**
	 * Converts a {@link JsonNode} to a {@link String} and wraps {@link JsonProcessingException} checked
	 * exception into an {@link IllegalArgumentException}.
	 *
	 * @param value node to convert
	 * @return string representation of the node
	 * @throws IllegalArgumentException if the conversion cannot be performed
	 */
	private static String writeValueAsString(final JsonNode value)
	{
		try
		{
			return MAPPER.writeValueAsString(value);
		}
		catch (final JsonProcessingException e)
		{
			throw new IllegalArgumentException(
					"Failed to write audit message. Unable to convert object to JSON string in preparation for audit logging.", e);
		}
	}

	/**
	 * Wrapper for {@link ObjectMapper#valueToTree(Object)} method that uses the {@link ObjectMapper} instance of this class.
	 *
	 * @param value object to be converted into a JSON Tree
	 * @return the JSON Tree representation of the object
	 */
	public static JsonNode valueToTree(final Object value)
	{
		return MAPPER.valueToTree(value);
	}

	public static ObjectMapper getMapper()
	{
		return MAPPER;
	}

	/**
	 * Generates a list of attribute keys from a JsonNode object with dot (.) nested attribute representation.
	 * Ex: Parent.child.grandchild
	 * @param node JsonNode Object from which we want to find keys
	 * @return A list of attribute keys from the object
	 */
	public static List<String> getKeys(JsonNode node)
	{
		final ArrayList<String> keys = new ArrayList<>();
		traverseKeys("", node, keys);
		return keys;
	}

	/**
	 * JsonNode tree traversal
	 * Recursively traverses a JsonNode object and populates an arraylist with key attributes
	 */
	private static void traverseKeys(String key, JsonNode node, List<String> attrList)
	{
		// Base Case: Leaf Node
		if (node.isValueNode())
		{
			if (key.isEmpty())
			{
				key = node.textValue();
			}
			attrList.add(key);
		}
		else if (node.isArray())
		{
			attrList.add(key + JSON_NODE_LIST_ATTRIBUTE_SUFFIX);
		}
		// Iterate through member attributes
		else if (node.isObject())
		{
			final ObjectNode objectNode = (ObjectNode) node;
			final Iterator<Map.Entry<String, JsonNode>> jsonIter = objectNode.fields();
			key = key.isEmpty() ? "" : key + ".";
			while (jsonIter.hasNext())
			{
				final Map.Entry<String, JsonNode> entry = jsonIter.next();
				traverseKeys(key + entry.getKey(), entry.getValue(), attrList);
			}
		}
	}
}
