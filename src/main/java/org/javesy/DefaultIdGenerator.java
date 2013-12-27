package org.javesy;

import java.util.concurrent.atomic.AtomicLong;

public class DefaultIdGenerator
    implements EntityIdGenerator
{
    /**
     * Entity id counter.
     */
    private AtomicLong entityCount = new AtomicLong(0);

    @Override
    public Long getNextEntityId()
    {
        return Long.valueOf( entityCount.getAndIncrement());
    }
}
