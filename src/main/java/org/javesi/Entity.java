package org.javesi;

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
public final class Entity
{
    private final long id;

    /** Mutable per entity flag to keep track of entity life time with just the entity reference.
     * <p>
     *     Not sure if I keep this. It seems the lesser evil compared to the idea of everyone having
     *     to have their entity aliveness checked by system hash tables.
     * </p>
     * <p>
     *     this is set by {@link EntitySystem#killEntity(Entity)}
     * </p>
     */
    private boolean alive;

    Entity(long id)
    {
        this.id = id;
        alive = true;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Entity)
        {
            Entity that = (Entity) obj;
            return id == that.id;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >> 32));
    }

    /**
     * Returns <code>true</code> as long as this entity has not been killed from its
     * entity system.
     *
     * @return
     */
    public boolean isAlive()
    {
        return alive;
    }

    void setAlive(boolean alive)
    {
        this.alive = alive;
    }

    public long getId()
    {
        return id;
    }
}
