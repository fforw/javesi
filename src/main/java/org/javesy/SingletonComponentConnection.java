package org.javesy;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Immutable class to encapsulate the attribution of a singleton component to an entity.
 *
 */
class SingletonComponentConnection
{
    public final SingletonComponent component;
    public final Entity entity;

    private ComponentSetView componentSet;
    private EntitySetView entitySet = new EntitySetView();

    public SingletonComponentConnection(SingletonComponent component, Entity entity)
    {
        this.component = component;
        this.entity = entity;
    }

    public Set<Component> components()
    {
        ComponentSetView set = componentSet;

        if (set == null)
        {
            set = new ComponentSetView();
            componentSet = set;
        }

        return set;
    }

    public Set<Entity> entities()
    {
        return entitySet;
    }

    private class ComponentSetView extends AbstractSetView<Component>
    {
        public Component getItem()
        {
            return component;
        }
    }

    private class EntitySetView extends AbstractSetView<Entity>
    {
        public Entity getItem()
        {
            return entity;
        }
    }

    private static abstract class AbstractSetView<T> extends AbstractSet<T>
    {

        abstract T getItem();

        @Override
        public Iterator<T> iterator()
        {
            return new Iterator<T>()
            {
                private boolean next = true;

                @Override
                public boolean hasNext()
                {
                    return next;
                }

                @Override
                public T next()
                {
                    if (next)
                    {
                        next = false;
                        return getItem();
                    }
                    throw new NoSuchElementException();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size()
        {
            return 1;
        }
    }
}
