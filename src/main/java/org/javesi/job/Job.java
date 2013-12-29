package org.javesi.job;

import org.javesi.EntitySystem;

/**
 * Implemented by code wanting to interact with an EntitySystem.
 *
 * Executes a unit of work with exclusive access to the entity system state.
 *
 */
public interface Job
{
    /**
     * Executes the org.javesi.job with the given input.
     * @param state
     * @throws Exception
     */
    void execute(EntitySystem state) throws Exception;
}
