package org.javesi.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates new identities on base of an atomic long.
 */
public final class DefaultIdGenerator
    implements EntityIdGenerator
{
    /**
     * Entity id counter.
     */
    private AtomicLong entityCount = new AtomicLong(0);

    @Override
    public long getNextEntityId()
    {
        return entityCount.getAndIncrement();
    }
}
