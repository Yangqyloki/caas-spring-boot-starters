package com.hybris.caas.multitenant.jpa;

import javax.persistence.EntityManager;

/**
 * Interface to be implemented by a class, such as a service, that requires the "eclipselink.tenant-id" property be
 * set for the {@link EntityManager} after the transaction has been started.
 */
public interface EntityManagerHolder
{
	/**
	 * Returns the {@link EntityManager} associated with the started transaction.
	 *
	 * @return the entity manager
	 */
	EntityManager getEntityManager();
}
