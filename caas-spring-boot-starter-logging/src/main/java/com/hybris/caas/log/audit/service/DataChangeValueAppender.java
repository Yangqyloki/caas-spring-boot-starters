package com.hybris.caas.log.audit.service;

/**
 * Functional interface for appending values/attributes to an audit message that
 * required a data change object from old value to new value.
 */
@FunctionalInterface
interface DataChangeValueAppender
{
	/**
	 * Method to append a single value/attribute change to the audit message.
	 *
	 * @param name     the name of the value/attribute that was changed
	 * @param oldValue the old value of the value/attribute
	 * @param newValue the new value of the value/attribute
	 */
	void appendValues(String name, String oldValue, String newValue);
}
