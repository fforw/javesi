package org.javesy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Encapsulates an entity system with a fixed number of known components.
 */
public final class EntitySystem
{
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntitySystem.class);

    private final static String UNNAMED = "[no name]";

    private Class[] componentTypesInHashOrder;

    /**
     * Array of Maps. Index is the index position in componentTypesInHashOrder, the maps map
     * all entities having that component type to the component of that type.
     */
    private ConcurrentMap<Entity, Component> componentStore[];

    /**
     * Maps entities to their names or <code>null</code>. primary tracker of existing entities.
     */
    private ConcurrentMap<Entity, String> entitiesToNames;

    /**
     * Id generator to be used for this system. Can be changed by calling {@link #setEntityName(Entity, String)} before
     * adding any entities.
     */
    private EntityIdGenerator idGenerator;

    /**
     * Constructs a new entity system from a set of component classes with the additional restriction that the
     * hash codes of all classes must be unique.
     *
     * @throws ComponentHashNotUniqueException if there are non-unique hash codes
     */
    public EntitySystem(EntitySystemConfig config)
    {
        try
        {
            this.idGenerator = config.getIdGenerator();

            Set<Class<? extends Component>> componentClasses = config.getComponentClasses();
            final int numberOfTypes = componentClasses.size();

            componentTypesInHashOrder = new Class[numberOfTypes];
            componentStore = new ConcurrentMap[numberOfTypes];

            entitiesToNames = new ConcurrentHashMap<Entity, String>(config.getInitialEntityCapacity());

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
                componentStore[index] = new ConcurrentHashMap<Entity, Component>(config.getInitialComponentCapacity());

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


    public int getTypeIndex(Class<? extends Component> componentClass)
    {
        int index = Arrays.binarySearch(componentTypesInHashOrder, componentClass, HashOrderComparator.INSTANCE);
        if (index < 0)
        {
            throw new IllegalStateException(componentClass + " is not known by this system");
        }
        return index;
    }

    public void killEntity(Entity entity)
    {
        String name = entitiesToNames.remove(entity);

        assert name != null : "Entity " + entity + " not found.";

        for (Map<Entity, ? extends Component> componentMap : componentStore)
        {
            componentMap.remove(entity);
        }

    }

    public <T extends Component> void removeComponent(Entity entity, Class<T> cls)
    {
        assert entitiesToNames.containsKey(entity): "Entity " + entity + " not found.";

        int index = getTypeIndex(cls);
        componentStore[index].remove(entity);
    }

    public <T extends Component> T getComponent(Entity entity, Class<T> componentType)
    {

        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        int index = getTypeIndex(componentType);
        return (T) componentStore[index].get(entity);
    }


    // internal test method. Is allowed to require on non-existing entities to validate component removal.
    <T extends Component> T getComponentInternal(Entity entity, Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        return (T) componentStore[index].get(entity);
    }


    public void setEntityName(Entity entity, String name)
    {
        if (name == null)
        {
            name = UNNAMED;
        }

        String oldName = entitiesToNames.replace(entity, name);

        // there are no null names in our map, so a null means "No entity" here.
        assert oldName != null :"Entity " + entity + " does not exist.";
    }

    public <T extends Component> boolean hasComponent(Entity entity,
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
    public List<? extends Component> getAllComponentsOnEntity(Entity entity)
    {
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        List<Component> components = new ArrayList<Component>(componentStore.length);

        for (ConcurrentMap<Entity, ? extends Component> map : componentStore)
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

    public <T extends Component> Set<Entity> findEntitiesWithComponent(
        Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        return (Set<Entity>) componentStore[index].keySet();
    }

    public Set<Entity> findEntitiesWithComponents(
        Class<? extends Component>... componentTypes)
    {

        if (componentTypes.length == 0)
        {
            return entities();
        }

        Set<Entity> matchedAll = null;
        for (Class componentType : componentTypes)
        {
            int index = getTypeIndex(componentType);
            Set<Entity> entitiesForComponent = (Set<Entity>) componentStore[index].keySet();
            if (matchedAll == null)
            {
                matchedAll = new HashSet<Entity>(entitiesForComponent);
            }
            else
            {
                matchedAll.retainAll(entitiesForComponent);
            }
        }
        return matchedAll;
    }

    public <T extends Component> void addComponent(Entity entity, T component)
    {
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        int index = getTypeIndex(component.getClass());
        componentStore[index].put(entity, component);
    }

    public Entity createEntity()
    {
        return createAndRegisterEntity(UNNAMED);
    }

    public Entity createNamedEntity(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Must have a name");
        }

        return createAndRegisterEntity(name);
    }

    public String nameFor(Entity entity)
    {
        String name = entitiesToNames.get(entity);

        assert name != null : "Entity" + entity + "does not exist";

        return name;
    }

    public Set<Entity> entities()
    {
        return Collections.unmodifiableSet(entitiesToNames.keySet());
    }

    private Entity createAndRegisterEntity(String name)
    {
        Entity entity = new Entity( idGenerator.getNextEntityId() );
        entitiesToNames.put(entity, name);
        return entity;
    }

    /**
     * Registers an alternate id generator for use with this entity system.
     *
     * @param idGenerator
     */

    public void setIdGenerator(EntityIdGenerator idGenerator)
    {
        if (entitiesToNames.size() > 0)
        {
            throw new IllegalStateException("Can't change id generator with existing entities.");
        }

        this.idGenerator = idGenerator;
    }
}
