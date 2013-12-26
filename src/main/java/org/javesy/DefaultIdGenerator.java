package org.javesy;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultIdGenerator
    implements EntityIdGenerator
{
    /**
     * Entity id counter.
     */
    private AtomicInteger entityCount = new AtomicInteger(0);

    @Override
    public int getNextEntityId()
    {
        return entityCount.getAndIncrement();
    }
}
