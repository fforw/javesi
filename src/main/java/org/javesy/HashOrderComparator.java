package org.javesy;

import java.util.Comparator;

class HashOrderComparator implements Comparator
{

    final static HashOrderComparator INSTANCE = new HashOrderComparator();

    private HashOrderComparator()
    {

    }

    @Override
    public int compare(Object o1, Object o2)
    {
        return o1.hashCode() - o2.hashCode();
    }
}
