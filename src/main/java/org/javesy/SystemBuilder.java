package org.javesy;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class SystemBuilder
{
    private EntityIdGenerator idGenerator = new DefaultIdGenerator();

    private int initialEntityCapacity = 10000;

    public SystemBuilder()
    {

    }

    public EntityIdGenerator getIdGenerator()
    {
        return idGenerator;
    }

    public SystemBuilder withIdGenerator(EntityIdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }

    public int getInitialEntityCapacity()
    {
        return initialEntityCapacity;
    }

    public SystemBuilder withInitialEntityCapacity(int initialEntityCapacity)
    {
        this.initialEntityCapacity = initialEntityCapacity;
        return this;
    }

    public EntitySystem buildFromPackage(String pkg)
    {

        try
        {
            Set<Class<? extends Component>> componentTypes = findComponentClasses(pkg);
            return buildFromComponentClasses(componentTypes);
        }
        catch (Exception e)
        {
            throw new JavesyRuntimeException(e);
        }
    }

    Set<Class<? extends Component>> findComponentClasses(String pkg)
    {
        // doing this the funny reflexive way keeps the reflections dependency optional.
        try
        {
            Class<?> reflectionsClass = Class.forName("org.reflections.Reflections");
            Object instance = reflectionsClass.getConstructor(Object[].class).newInstance((Object)new Object[]{ pkg });
            return (Set<Class<? extends Component>>) reflectionsClass.getMethod("getSubTypesOf", Class.class ).invoke(instance, Component.class);
        }
        catch (Exception e)
        {
            throw new JavesyRuntimeException(e);
        }
    }

    public EntitySystem buildFromComponentClasses(Set<Class<? extends Component>> pkg)
    {
        return new EntitySystem(pkg, idGenerator, initialEntityCapacity);
    }
}
