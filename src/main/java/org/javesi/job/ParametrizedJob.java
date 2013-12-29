package org.javesi.job;

import org.javesi.EntitySystem;

/**
 * Implemented by code wanting to interact with an EntitySystem.
 *
 * Executes a unit of work with exclusive access to the entity system state.
 *
 * The input will reflect the input value {@link org.javesi.EntitySystemInterface#execute(ParametrizedJob, Object)} was called with.
 *
 * @param <I> input type
 */
public interface ParametrizedJob<I>
{
    /**
     * Executes the org.javesi.job with the given input.
     * @param state
     * @param input
     * @throws Exception
     */
    void execute(EntitySystem state, I input) throws Exception;
}
