package com.hybris.caas.error.util;

import com.hybris.caas.error.exception.UpdateConflictException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility to detect conflicts during service level updates. The detection process will throw a
 * {@link UpdateConflictException} if a conflict is detected.
 * <p>
 * See DSL usage pattern below:
 * <pre>
 * UpdateConflictDetector.withAttribute("type").withValues(entity.getType(), dto.getType())
 * 	.andAttribute("collectionId").withValues(entity.getCollectionId(), dto.getCollectionId())
 * 	.detect();
 * </pre>
 */
public final class UpdateConflictDetector
{
	final Map<String, Object[]> items = new HashMap<>();

	private UpdateConflictDetector()
	{
		// private constructor
	}

	/**
	 * Create a new {@link UpdateConflictDetectorBuilder} with the provided attribute name.
	 *
	 * @param attributeName the name of the attribute on which to perform conflict detection
	 * @return a new builder
	 */
	public UpdateConflictDetectorBuilder andAttribute(final String attributeName)
	{
		if (Objects.isNull(attributeName))
		{
			throw new IllegalArgumentException("Attribute name cannot be null.");
		}
		return new UpdateConflictDetectorBuilder(this, attributeName);
	}

	/**
	 * Detect any conflicts among the attribute values provided.
	 *
	 * @throws UpdateConflictException when a conflict is detected
	 */
	public void detect()
	{
		final Map<String, Object[]> conflicts = items.entrySet().stream().filter(entrySet -> !nullSafeEquals(entrySet.getValue()[0], entrySet.getValue()[1]))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		if (!conflicts.isEmpty())
		{
			throw new UpdateConflictException(conflicts);
		}
	}

	/**
	 * Null-safe object equality verification.
	 *
	 * @param obj1 the first object
	 * @param obj2 the second object
	 * @return <code>true</code> if the objects are equal; <code>false</code> otherwise
	 */
	private static boolean nullSafeEquals(final Object obj1, final Object obj2)
	{
		if (Objects.isNull(obj1) && Objects.isNull(obj2))
		{
			return Boolean.TRUE;
		}
		else if (Objects.isNull(obj1) || Objects.isNull(obj2))
		{
			return Boolean.FALSE;
		}
		else
		{
			return obj1.equals(obj2);
		}
	}

	/**
	 * Create a new {@link UpdateConflictDetector} and then a new {@link UpdateConflictDetectorBuilder} with the provided attribute name.
	 *
	 * @param attributeName the name of the attribute on which to perform conflict detection
	 * @return a new builder
	 */
	public static UpdateConflictDetectorBuilder withAttribute(final String attributeName)
	{
		final UpdateConflictDetector detector = new UpdateConflictDetector();
		return detector.andAttribute(attributeName);
	}

	/**
	 * Builder used to provide attribute values in a DSL-like manner.
	 */
	public final class UpdateConflictDetectorBuilder
	{
		private final UpdateConflictDetector detector;
		private final String attributeName;

		private UpdateConflictDetectorBuilder(final UpdateConflictDetector detector, final String attributeName)
		{
			this.detector = detector;
			this.attributeName = attributeName;
		}

		/**
		 * Add the values to use when verifying conflicts for the given attribute.
		 *
		 * @param source the source value
		 * @param target the target value
		 * @return the original detector object to continue adding more attributes
		 */
		public <T> UpdateConflictDetector withValues(final T source, final T target)
		{
			items.put(attributeName, new Object[] {source, target});
			return detector;
		}
	}
}
