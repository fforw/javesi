package org.javesi;

import org.javesi.job.Job;
import org.javesi.job.ParametrizedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Offers a methods to execute {@link Job}s and {@link ParametrizedJob}s on your entity system.
 * <p>
 * If your game is simple, you can just wrap your main loop in a org.javesi.job.
 * </p>
 * <p>
 * If you want to fully use multiple CPUs you might have to weave a complicated net of short-running
 * jobs to interface between your concurrent subsystems.
 * </p>
 */
public final class EntitySystemInterface
{
    private static Logger log = LoggerFactory.getLogger(EntitySystemInterface.class);
    private final EntitySystem system;
    private final Object worldLock;


    EntitySystemInterface(EntitySystem system)
    {
        this.system = system;
        this.worldLock = new Object();
    }

    /**
     * Executes the given org.javesi.job while obtaining a lock on the internal entity system.
     *
     * @param job
     */
    public void execute(Job job) throws Exception
    {
        synchronized (worldLock)
        {
            job.execute(system);
        }
    }

    /**
     * Executes the given org.javesi.job with the given input while obtaining a lock on the internal entity system.
     *
     * @param job       org.javesi.job to execute
     * @param input     input as declared / needed by the org.javesi.job
     * @param <I>       type of the input
     * @throws Exception
     */
    public <I> void execute(ParametrizedJob<I> job, I input) throws Exception
    {
        synchronized (worldLock)
        {
            job.execute(system, input);
        }
    }
}
