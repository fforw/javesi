package org.javesi;

/**
 * Executes a unit of work with exclusive access to the entity system state.
 */
public interface Job
{
    void execute(EntitySystem state) throws Exception;
}
