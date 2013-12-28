package org.javesy.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Specialized set implementation with a single member value.
 *
 * @param <T> type of the one and only member of the set
 */
public final class SingletonSet<T> extends AbstractSet<T>
{
    private final T value;

    /**
     * Constructs a new set for the given value.
     *
     * @param value
     */
    public SingletonSet(T value)
    {
        this.value = value;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new SingletonSetIterator(value);
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
