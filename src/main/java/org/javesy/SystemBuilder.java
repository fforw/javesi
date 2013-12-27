package org.javesy;

import java.util.Set;

public class SystemBuilder implements EntitySystemConfig
{
    private Set<Class<? extends Component>> componentClasses;

    private EntityIdGenerator idGenerator;

    private int initialEntityCapacity = 10000;

    private int initialComponentCapacity = 5000;

    public SystemBuilder()
    {
        idGenerator = new DefaultIdGenerator();
    }

    @Override
    public EntityIdGenerator getIdGenerator()
    {
        return idGenerator;
    }

    //// WITHER METHODS ////////////////////////////

    public SystemBuilder withIdGenerator(EntityIdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }

    public SystemBuilder withInitialComponentCapacity(int initialComponentCapacity)
    {
        this.initialComponentCapacity = initialComponentCapacity;

        return this;
    }

    public SystemBuilder withInitialEntityCapacity(int initialEntityCapacity)
    {
        this.initialEntityCapacity = initialEntityCapacity;
        return this;
    }

    //// GETTER METHODS ////////////////////////////

    @Override
    public int getInitialEntityCapacity()
    {
        return initialEntityCapacity;
    }

    @Override
    public int getInitialComponentCapacity()
    {
        return initialComponentCapacity;
    }

    @Override
    public Set<Class<? extends Component>> getComponentClasses()
    {
        return componentClasses;
    }

    ///////////////////////////////////////////////////////////////////////

    Set<Class<? extends Component>> findComponentClasses(String pkg)
    {
        try
        {
            // doing this the funny reflexive way keeps the reflections dependency optional.
            Class<?> reflectionsClass = Class.forName("org.reflections.Reflections");

            // the elusive cast to Object ---------------------------------------------------.
            //                                                                               v
            Object instance = reflectionsClass.getConstructor(Object[].class).newInstance((Object)new Object[]{ pkg });
            return (Set<Class<? extends Component>>) reflectionsClass.getMethod("getSubTypesOf", Class.class ).invoke(instance, Component.class);
        }
        catch (Exception e)
        {
            throw new JavesyRuntimeException(e);
        }
    }

    //// BUILDER METHODS ////////////////////////////

    public EntitySystem buildFromPackage(String pkg)
    {

        try
        {
            this.componentClasses = findComponentClasses(pkg);

            return new EntitySystem(this);
        }
        catch (Exception e)
        {
            throw new JavesyRuntimeException(e);
        }
    }


    public EntitySystem buildFromComponentClasses(Set<Class<? extends Component>> pkg)
    {
        this.componentClasses = pkg;

        return new EntitySystem(this);
    }
}
