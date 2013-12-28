package org.javesy.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SingletonSet<T> extends AbstractSet<T>
{
    private final T value;

    public SingletonSet(T value)
    {
        this.value = value;
    }

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
                    return value;
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

    public T getValue()
    {
        return value;
    }

    @Override
    public int size()
    {
        return 1;
    }
}
