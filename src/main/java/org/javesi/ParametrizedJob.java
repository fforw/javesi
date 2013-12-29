package org.javesi;

/**
 * Implemented by code wanting to interact with an EntitySystem.
 *
 * Executes a unit of work with exclusive access to the entity system state.
 *
 * The input will reflect the input value {@link org.javesi.EntitySystemInterface#execute(org.javesi.ParametrizedJob, Object)} was called with.
 * It will be <code >null</code> if the invoked by the no-input {@link org.javesi.EntitySystemInterface#execute(org.javesi.ParametrizedJob)}.
 *
 * @param <I> input type, can be {@link Void}
 */
public interface ParametrizedJob<I>
{
    /**
     * Executes the job with the given input.
     * @param state
     * @param input
     * @throws Exception
     */
    void execute(EntitySystem state, I input) throws Exception;
}
