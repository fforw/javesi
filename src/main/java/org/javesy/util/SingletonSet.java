package org.javesy.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingletonSet<T> extends AbstractSet<T>
{
    protected final T value;

    public SingletonSet(T value)
    {
        this.value = value;
    }

    public T getItem()
    {
        return value;
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
