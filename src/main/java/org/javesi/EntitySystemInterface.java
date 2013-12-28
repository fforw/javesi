package org.javesi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Offers a method to execute {@link Job}s on your entity system.
 * <p>
 *     If your game is simple, you can just wrap your main loop in a job.
 * </p>
 * <p>
 *     If you want to fully use multiple CPUs you might have to weave a complicated net of short-running
 *     jobs to interface between your concurrent subsystems.
 * </p>
 */
public final class EntitySystemInterface
{
    private final EntitySystem system;

    private final Object worldLock;

    private static Logger log = LoggerFactory.getLogger(EntitySystemInterface.class);


    EntitySystemInterface(EntitySystem system)
    {
        this.system = system;
        this.worldLock = new Object();
    }

    /**
     * Executes the given job while obtaining a lock on the given entity system.
     * @param job
     */
    public void execute(Job job) throws Exception
    {
        synchronized (worldLock)
        {
            job.execute(system);
        }
    }
}
