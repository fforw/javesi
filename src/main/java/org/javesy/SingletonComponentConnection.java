package org.javesy;

import org.javesy.util.SingletonSet;

import java.util.Set;

/**
 * Immutable class to encapsulate the attribution of a singleton component to an entity.
 *
 * Offers two set views on the component and entity via lazy initialized {@link SingletonSet] impl.
 */
class SingletonComponentConnection
{
    public final SingletonComponent component;
    public final Entity entity;

    private SingletonSet<Component> componentSet;
    private SingletonSet<Entity> entitySet;

    public SingletonComponentConnection(SingletonComponent component, Entity entity)
    {
        assert entity != null : "Entity can't be null";
        assert component != null : "Component can't be null";

        this.component = component;
        this.entity = entity;
    }

    public Set<Component> components()
    {
        SingletonSet<Component> set = componentSet;

        if (set == null)
        {
            set = new SingletonSet<Component>(this.component);
            componentSet = set;
        }
        return set;
    }

    public Set<Entity> entities()
    {
        SingletonSet<Entity> set = entitySet;

        if (set == null)
        {
            set = new SingletonSet<Entity>(this.entity);
            entitySet = set;
        }

        return set;
    }
}
