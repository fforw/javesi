package org.javesy;

/**
 * Represents an entity.
 * <p>
 *     This is a class for several reasons. As tempting as it might seem to express it as a primitive it's a bad idea because
 *     the entity id is not a number in the sense that you would calculate with it. And to use it for its primary reference
 *     purpose, you'd have to (auto)wrap it in a Long anyway.
 * </p>
 * <p>
 *     In addition to only doing the wrapping once, this class is also type-safe, in contrast to a Long.
 * </p>
 *
 */
public class Entity
{
    public final long id;

    Entity(Long id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Entity)
        {
            return id == ((Entity)obj).id;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return (int)((id >> 32) ^ (id & 0xffffffffL));
    }
}
