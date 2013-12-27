package org.javesy;

import org.javesy.id.EntityIdGenerator;

import java.util.Set;

/**
 * Encapsulates all configuration options for an entity system.
 */
interface EntitySystemConfig
{
    Set<Class<? extends Component>> getComponentClasses();

    EntityIdGenerator getIdGenerator();

    int getInitialEntityCapacity();

    int getInitialComponentCapacity();
}
