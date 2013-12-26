package org.javesy;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulates an entity system with a fixed number of known components.
 */
public final class EntitySystem
{
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntitySystem.class);

    private Class[] componentTypesInHashOrder;

    /**
     * Array of Maps. Index is the index position in componentTypesInHashOrder, the maps map
     * all entities having that component type to the component of that type.
     */
    private ConcurrentMap<Integer, Component> componentStore[];

    /**
     * Maps entities to their names or <code>null</code>. primary tracker of existing entities.
     */
    private ConcurrentMap<Integer, String> entitiesToNames;

    /**
     * Entity id counter.
     */
    private AtomicInteger entityCount = new AtomicInteger(0);

    /**
     * Constructs a new entity system from a set of component classes with the additional restriction that the
     * hash codes of all classes must be unique.
     *
     * @param componentClasses
     * @throws ComponentHashNotUniqueException if there are non-unique hash codes
     */
    private EntitySystem(Set<Class<? extends Component>> componentClasses)
    {
        try
        {
            final int numberOfTypes = componentClasses.size();
            componentTypesInHashOrder = new Class[numberOfTypes];
            componentStore = new ConcurrentMap[numberOfTypes];

            entitiesToNames = new ConcurrentHashMap<Integer, String>();

            Map<Integer,Class<? extends Component>> knownHashes = new HashMap<Integer,Class<? extends Component>>();

            int index = 0;
            for (Class<? extends Component> componentClass : componentClasses)
            {
                int componentHash = componentClass.hashCode();

                Class<? extends Component> otherComponentClass = knownHashes.get(componentHash);

                if (otherComponentClass != null)
                {
                    throw new ComponentHashNotUniqueException("The component " + componentClass + " and " + otherComponentClass + " have the same hashCode. " +
                        "This unfortunately violates one of our requirements. I'm afraid I have to ask you to change *something* on these classes.");
                }

                Component instance = componentClass.newInstance();
                componentTypesInHashOrder[index] = componentClass;
                componentStore[index] = new ConcurrentHashMap<Integer, Component>();

                knownHashes.put(componentHash, componentClass);

                index++;
            }

            Arrays.sort(componentTypesInHashOrder, HashOrderComparator.INSTANCE);
        }
        catch (InstantiationException e)
        {
            throw new JavesyRuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new JavesyRuntimeException(e);
        }
    }

    public static EntitySystem scanPackage(String pkg)
    {
        Reflections reflections = new Reflections(pkg);
        Set<Class<? extends Component>> componentTypes = reflections.getSubTypesOf(Component.class);
        return EntitySystem.forComponentClasses(componentTypes);
    }

    public static EntitySystem forComponentClasses(Set<Class<? extends Component>> pkg)
    {
        return new EntitySystem(pkg);
    }

    public int getTypeIndex(Class<? extends Component> componentClass)
    {
        int index = Arrays.binarySearch(componentTypesInHashOrder, componentClass, HashOrderComparator.INSTANCE);
        if (index < 0)
        {
            throw new IllegalStateException(componentClass + " is not known by this system");
        }
        return index;
    }

    public void killEntity(int entity)
    {
        for (Map<Integer, ? extends Component> componentMap : componentStore)
        {
            componentMap.remove(entity);
        }

        entitiesToNames.remove(entity);
    }

    public <T extends Component> void removeComponent(int entity, Class<T> cls)
    {
        int index = getTypeIndex(cls);
        componentStore[index].remove(entity);
    }

    public <T extends Component> T getComponent(int entity,
                                                Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        return (T) componentStore[index].get(entity);
    }


    public void setEntityName(int entity, String name)
    {
        entitiesToNames.put(entity, name);
    }

    public String nameFor(int entity)
    {
        return entitiesToNames.get(entity);
    }

    public <T extends Component> boolean hasComponent(int entity,
                                                      Class<T> componentType)
    {
        return getComponent(entity, componentType) != null;
    }


    /**
     * Slow convenience methods to get all components on a certain entity.
     *
     * Components are returned in hash code order.
     *
     * @param entity
     * @return
     */
    public List<? extends Component> getAllComponentsOnEntity(int entity)
    {
        List<Component> components = new ArrayList<Component>(componentStore.length);

        for (ConcurrentMap<Integer, ? extends Component> map : componentStore)
        {
            Component c = map.get(entity);
            if (c != null)
            {
                components.add(c);
            }
        }

        return components;
    }


    public <T extends Component> Collection<T> getAllComponentsOfType(
        Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        return (Collection<T>) componentStore[index].values();
    }

    public <T extends Component> Set<Integer> findEntitiesWithComponent(
        Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        return (Set<Integer>) componentStore[index].keySet();
    }

    public Set<Integer> findEntitiesWithComponents(
        Class<? extends Component>... componentTypes)
    {

        if (componentTypes.length == 0)
        {
            return entities();
        }

        Set<Integer> matchedAll = null;
        for (Class componentType : componentTypes)
        {
            int index = getTypeIndex(componentType);
            Set<Integer> entitiesForComponent = (Set<Integer>) componentStore[index].keySet();
            if (matchedAll == null)
            {
                matchedAll = new HashSet<Integer>(entitiesForComponent);
            }
            else
            {
                matchedAll.retainAll(entitiesForComponent);
            }
        }
        return matchedAll;
    }

    public <T extends Component> void addComponent(int entity, T component)
    {
        int index = getTypeIndex(component.getClass());
        componentStore[index].put(entity, component);
    }

    public int createEntity()
    {
        return createAndRegisterEntity(null);
    }

    public int createNamedEntity(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Must have a name");
        }

        return createAndRegisterEntity(name);
    }

    public Set<Integer> entities()
    {
        return entitiesToNames.keySet();
    }

    private int createAndRegisterEntity(String name)
    {
        int entity = entityCount.getAndIncrement();
        entitiesToNames.put(entity, name);
        return entity;
    }
}
