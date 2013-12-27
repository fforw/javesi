package org.javesy;

import org.javesy.exception.ComponentHashNotUniqueException;
import org.javesy.id.EntityIdGenerator;
import org.javesy.util.HashOrderComparator;

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
 * Encapsulates an entity system with a fixed number of known components. The entity system encompasses the knowledge
 * about the identities of all game objects in an entity system / world.
 * <p>
 *     There is a fixed set of possible components that encapsulate all the data to represent a certain aspect of that
 *     game object. Entities can be assigned as many components as necessary.
 * </p>
 * <p>
 *     The actual work should be done in a {@link org.javesy.subsystem.SubSystemService} or in your own sub service
 *     infrastructure.
 * </p>
 */
public final class EntitySystem
{
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntitySystem.class);

    private final static String UNNAMED = "[no name]";

    private Class<? extends Component>[] componentTypesInHashOrder;

    /**
     * Id generator to be used for this system. Can be changed by calling {@link #setEntityName(Entity, String)} before
     * adding any entities.
     */
    private final EntityIdGenerator idGenerator;

    private final int numberOfComponentTypes;

    /**
     * Array of Maps. Index is the index position in componentTypesInHashOrder, the maps map
     * all entities having that component type to the component of that type.
     */
    private final ConcurrentMap<Entity, Component> componentStore[];


    private final SingletonComponentConnection[] singletonConnections;

    /**
     * Maps entities to their names or <code>null</code>. primary tracker of existing entities.
     */
    private final ConcurrentMap<Entity, String> entitiesToNames;


    /**
     * Constructs a new entity system from a set of component classes with the additional restriction that the
     * hash codes of all classes must be unique.
     *
     * @throws org.javesy.exception.ComponentHashNotUniqueException if there are non-unique hash codes
     */
    public EntitySystem(EntitySystemConfig config)
    {
        this.idGenerator = config.getIdGenerator();

        Set<Class<? extends Component>> componentClasses = config.getComponentClasses();
        numberOfComponentTypes = componentClasses.size();

        componentTypesInHashOrder = new Class[numberOfComponentTypes];
        componentStore = new ConcurrentMap[numberOfComponentTypes];
        singletonConnections = new SingletonComponentConnection[numberOfComponentTypes];

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
            componentTypesInHashOrder[index++] = componentClass;
            knownHashes.put(componentHash, componentClass);
        }

        Arrays.sort(componentTypesInHashOrder, HashOrderComparator.INSTANCE);

        for (int i=0; i < numberOfComponentTypes ; i++)
        {
            Class<? extends Component> componentType = componentTypesInHashOrder[i];

            // initialize map for non-singletons. also acts as signal later
            if (!SingletonComponent.class.isAssignableFrom(componentType))
            {
                componentStore[i] = new ConcurrentHashMap<Entity, Component>(config.getInitialComponentCapacity());
            }
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
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert name != null : "Entity " + entity + " not found.";

        entity.alive = false;

        for (int i=0; i < numberOfComponentTypes; i++)
        {
            ConcurrentMap<Entity, Component> map = componentStore[i];
            if (map == null)
            {
                SingletonComponentConnection connection = singletonConnections[i];
                if (connection != null && connection.entity.id == entity.id )
                {
                    singletonConnections[i] = null;
                }
            }
            else
            {
                map.remove(entity);
            }
        }
    }

    public <T extends Component> void removeComponent(Entity entity, Class<T> componentType)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity): "Entity " + entity + " not found.";

        int index = getTypeIndex(componentType);

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            SingletonComponentConnection connection = singletonConnections[index];
            if (connection.entity.id == entity.id)
            {
                singletonConnections[index] = null;
            }
        }
        else
        {
            componentStore[index].remove(entity);
        }
    }

    public <T extends Component> T getComponent(Entity entity, Class<T> componentType)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        return getComponentInternal(entity, componentType);
    }


    // internal test method. Is allowed to require on non-existing entities to validate component removal.
    <T extends Component> T getComponentInternal(Entity entity, Class<T> componentType)
    {
        int index = getTypeIndex(componentType);

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            SingletonComponentConnection connection = singletonConnections[index];

            if (connection != null && connection.entity.id == entity.id)
            {
                return (T) connection.component;
            }

            return (T) null;
        }
        else
        {
            return (T) componentStore[index].get(entity);
        }
    }

    public void setEntityName(Entity entity, String name)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
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
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        List<Component> components = new ArrayList<Component>(numberOfComponentTypes);

        for (int i=0; i < numberOfComponentTypes; i++)
        {
            ConcurrentMap<Entity, ? extends Component> map = componentStore[i];
            // a null signals it never got initialized because it's a SingleComponent
            Component c;
            if (map == null)
            {
                SingletonComponentConnection connection = singletonConnections[i];
                c = connection != null ? connection.component : null;
            }
            else
            {
                c = map.get(entity);
            }

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

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            SingletonComponentConnection connection = singletonConnections[index];
            return (Collection<T>) (connection != null ? connection.components() : Collections.emptyList());
        }
        else
        {
            return (Collection<T>) componentStore[index].values();
        }
    }

    public <T extends SingletonComponent> Entity findEntityWithSingletonComponent(Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        SingletonComponentConnection connection = singletonConnections[index];
        return (Entity) (connection != null ? connection.entity : null);
    }

    public <T extends Component> Set<Entity> findEntitiesWithComponent(
        Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            SingletonComponentConnection connection = singletonConnections[index];
            return (Set<Entity>) (connection != null ? connection.entities() : Collections.emptySet());
        }
        else
        {
            return (Set<Entity>) componentStore[index].keySet();
        }
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
            Set<Entity> entitiesForComponent = findEntitiesWithComponent(componentType);
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
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        Class<? extends Component> componentType = component.getClass();
        int index = getTypeIndex(componentType);

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            singletonConnections[index] = new SingletonComponentConnection((SingletonComponent)component, entity);
        }
        else
        {
            componentStore[index].put(entity, component);
        }
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
        return entitiesToNames.get(entity);
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
}
