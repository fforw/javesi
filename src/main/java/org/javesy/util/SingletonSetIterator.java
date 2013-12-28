package org.javesy.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingletonSetIterator<T>
    implements Iterator<T>
{
    private T value;

    public SingletonSetIterator(T value)
    {
        this.value = value;
    }

    @Override
    public boolean hasNext()
    {
        return value != null;
    }

    @Override
    public T next()
    {
        T value = this.value;
        if (value != null)
        {
            this.value = null;
            return value;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
