package org.javesy;

import org.javesy.id.EntityIdGenerator;

public class ConstantIdGenerator
    implements EntityIdGenerator
{
    private long constantValue;

    public ConstantIdGenerator(long constantValue)
    {
        this.constantValue = constantValue;
    }

    @Override
    public long getNextEntityId()
    {
        return constantValue;
    }

    @Override
    public int hashCode()
    {
        return (int)(constantValue ^ (constantValue >> 32));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ConstantIdGenerator)
        {
            return constantValue == ((ConstantIdGenerator)obj).constantValue;
        }
        return false;
    }
}
