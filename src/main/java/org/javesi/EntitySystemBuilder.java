package org.javesi;

import org.javesi.exception.JavesyRuntimeException;
import org.javesi.id.DefaultIdGenerator;
import org.javesi.id.EntityIdGenerator;

import java.util.HashSet;
import java.util.Set;

public class EntitySystemBuilder
    implements EntitySystemConfig
{
    private Set<Class<? extends Component>> componentClasses;

    private EntityIdGenerator idGenerator;

    /** default entity map capacity */
    private int entityMapCapacity = 10000;
    /** default entity map load factor */
    private float entityMapLoadFactor = 0.75f;
    /** default entity map concurrency level */
    private int entityMapConcurrencyLevel = 16;

    /** default component map capacity */
    private int componentMapCapacity = 5000;
    /** default component map load factor */
    private float componentMapLoadFactor = 0.75f;
    /** default component map concurrency level */
    private int componentMapConcurrencyLevel = 16;

    public EntitySystemBuilder()
    {
        idGenerator = new DefaultIdGenerator();
    }

    @Override
    public EntityIdGenerator getIdGenerator()
    {
        return idGenerator;
    }

    //// WITHER METHODS ////////////////////////////

    public EntitySystemBuilder withComponentClasses(Set<Class<? extends Component>> componentClasses)
    {
        this.componentClasses = componentClasses;
        return this;
    }

    public EntitySystemBuilder withIdGenerator(EntityIdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }

    public EntitySystemBuilder withEntityMapCapacity(int entityMapCapacity)
    {
        this.entityMapCapacity = entityMapCapacity;
        return this;
    }

    public EntitySystemBuilder withComponentMapCapacity(int componentMapCapacity)
    {
        this.componentMapCapacity = componentMapCapacity;
        return this;
    }

    public EntitySystemBuilder withEntityMapLoadFactor(float entityMapLoadFactor)
    {
        this.entityMapLoadFactor = entityMapLoadFactor;
        return this;
    }

    public EntitySystemBuilder withEntityMapConcurrencyLevel(int entityMapConcurrencyLevel)
    {
        this.entityMapConcurrencyLevel = entityMapConcurrencyLevel;
        return this;
    }

    public EntitySystemBuilder withComponentMapConcurrencyLevel(int componentMapConcurrencyLevel)
    {
        this.componentMapConcurrencyLevel = componentMapConcurrencyLevel;
        return this;
    }

    public EntitySystemBuilder withComponentMapLoadFactor(float componentMapLoadFactor)
    {
        this.componentMapLoadFactor = componentMapLoadFactor;
        return this;
    }


    //// GETTER METHODS ////////////////////////////

    @Override
    public int getComponentMapCapacity()
    {
        return componentMapCapacity;
    }

    @Override
    public float getComponentMapLoadFactor()
    {
        return componentMapLoadFactor;
    }

    @Override
    public int getComponentMapConcurrencyLevel()
    {
        return componentMapConcurrencyLevel;
    }

    @Override
    public int getEntityMapConcurrencyLevel()
    {
        return entityMapConcurrencyLevel;
    }

    @Override
    public float getEntityMapLoadFactor()
    {
        return entityMapLoadFactor;
    }

    @Override
    public int getEntityMapCapacity()
    {
        return entityMapCapacity;
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


            Set<Class<? extends Component>> set = new HashSet<Class<? extends Component>>();
            set.addAll((Set<Class<? extends Component>>) reflectionsClass.getMethod("getSubTypesOf", Class.class ).invoke(instance, Component.class));
            set.addAll((Set<Class<? extends Component>>) reflectionsClass.getMethod("getSubTypesOf", Class.class ).invoke(instance, SingletonComponent.class));
            return set;
        }
        catch (Exception e)
        {
            throw new JavesyRuntimeException(e);
        }
    }



    public EntitySystemBuilder withComponentPackage(String pkg)
    {
        try
        {
            this.componentClasses = findComponentClasses(pkg);
            return this;
        }
        catch (Exception e)
        {
            throw new JavesyRuntimeException(e);
        }
    }


    public EntitySystemInterface build()
    {
        return new EntitySystem(this).getExecutor();
    }

    @Override
    public final boolean equals(Object obj)
    {
        // testing relies on instance equality of builder
        return super.equals(obj);
    }
}
