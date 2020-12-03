package com.hybris.caas.log.audit.service;

/**
 * Strategy for processing {@link DataChangeObject}. This will take the data
 * change object and a {@link DataChangeValueAppender} function and append the
 * old and new values to the audit message according to the strategy.
 */
@FunctionalInterface
public interface DataChangeObjectProcessor
{
	/**
	 * Process the data change object.
	 * 
	 * @param dataChangeObject the data change object
	 * @param appenderFunction the function to apply
	 * @throws AuditLoggingException if something goes wrong   
	 */
	void process(DataChangeObject dataChangeObject, DataChangeValueAppender appenderFunction);
	
}
