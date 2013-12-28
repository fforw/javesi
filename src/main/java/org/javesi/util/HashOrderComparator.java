package org.javesi.util;

import java.util.Comparator;
public final class HashOrderComparator implements Comparator
{

    public final static HashOrderComparator INSTANCE = new HashOrderComparator();

    private HashOrderComparator()
    {

    }

    @Override
    public int compare(Object o1, Object o2)
    {
        return o1.hashCode() - o2.hashCode();
    }
}
