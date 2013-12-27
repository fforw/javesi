package org.javesy;

import java.util.Set;

public interface EntitySystemConfig
{
    Set<Class<? extends Component>> getComponentClasses();

    EntityIdGenerator getIdGenerator();

    int getInitialEntityCapacity();

    int getInitialComponentCapacity();
}
