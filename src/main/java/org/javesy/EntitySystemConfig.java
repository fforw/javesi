package org.javesy;

import org.javesy.id.EntityIdGenerator;

import java.util.Set;

/**
 * Encapsulates all configuration options for an entity system.
 */
public interface EntitySystemConfig
{
    Set<Class<? extends Component>> getComponentClasses();

    EntityIdGenerator getIdGenerator();

    // entity map config
    int getEntityMapCapacity();
    float getEntityMapLoadFactor();
    int getEntityMapConcurrencyLevel();

    // config for each component map
    int getComponentMapCapacity();
    float getComponentMapLoadFactor();
    int getComponentMapConcurrencyLevel();

}
