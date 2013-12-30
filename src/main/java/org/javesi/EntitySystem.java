package org.javesi;

import org.javesi.component.Component;
import org.javesi.component.SingletonComponent;
import org.javesi.exception.ComponentHashNotUniqueException;
import org.javesi.exception.InvalidComponentTypeException;
import org.javesi.id.EntityIdGenerator;
import org.javesi.util.HashOrderComparator;
import org.javesi.util.SingletonSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Encapsulates an entity system with a fixed number of known components. The entity system encompasses the knowledge
 * about the identities of all game objects in an entity system / world.
 * <p>
 * There is a fixed set of possible components that encapsulate all the data to represent a certain aspect of that
 * game object. Entities can be assigned as many components as necessary.
 * </p>
 * <p>
 * The actual work should be done in a {@link org.javesi.subsystem.SubSystemService} or in your own sub service
 * infrastructure.
 * </p>
 */
public final class EntitySystem
{
    private final static String UNNAMED = "[no name]";

    private final static String SEP = System.getProperty("line.separator");

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntitySystem.class);
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
    private final Map<Entity, Component> componentStoreRO[];
    private final Collection<Component> componentValuesRO[];
    private final SingletonComponentConnection[] singletonConnections;
    private final Set<Entity> entitySetRO;
    /**
     * Maps entities to their names or <code>null</code>. primary tracker of existing entities.
     */
    private final ConcurrentMap<Entity, String> entitiesToNames;
    private Class<? extends Component>[] componentTypesInHashOrder;


    /**
     * Constructs a new entity system from a set of component classes with the additional restriction that the
     * hash codes of all classes must be unique.
     *
     * @throws org.javesi.exception.ComponentHashNotUniqueException
     *          if there are non-unique hash codes
     */
    public EntitySystem(EntitySystemConfig config)
    {
        this.idGenerator = config.getIdGenerator();

        Set<Class<? extends Component>> componentClasses = config.getComponentClasses();
        numberOfComponentTypes = componentClasses.size();

        componentStore = new ConcurrentMap[numberOfComponentTypes];
        singletonConnections = new SingletonComponentConnection[numberOfComponentTypes];
        componentStoreRO = new Map[numberOfComponentTypes];
        componentValuesRO = new Collection[numberOfComponentTypes];

        entitiesToNames = new ConcurrentHashMap<Entity, String>(config.getEntityMapCapacity(),
            config.getEntityMapLoadFactor(), config.getEntityMapConcurrencyLevel());
        ;
        entitySetRO = Collections.unmodifiableSet(entitiesToNames.keySet());

        componentTypesInHashOrder = getSortedComponentTypes(componentClasses);


        for (int i = 0; i < numberOfComponentTypes; i++)
        {
            Class<? extends Component> componentType = componentTypesInHashOrder[i];

            // initialize map for non-singletons. also acts as signal later
            if (!SingletonComponent.class.isAssignableFrom(componentType))
            {
                ConcurrentHashMap<Entity, Component> newMap = new ConcurrentHashMap<Entity,
                    Component>(config.getComponentMapCapacity(), config.getComponentMapLoadFactor(),
                    config.getComponentMapConcurrencyLevel());
                componentStore[i] = newMap;

                Map<Entity, Component> entityComponentMap = Collections.unmodifiableMap(newMap);
                componentStoreRO[i] = entityComponentMap;
                componentValuesRO[i] = Collections.unmodifiableCollection(newMap.values());
            }
        }
    }

    /**
     * Returns an object representing the current entity system status. Pretty-prints via toString()
     * @return
     */
    public EntitySystem.Status getStatus()
    {
        int entityMapSize = entitiesToNames.size();

        Map<String, Integer> componentMapSizes = new TreeMap<String, Integer>();

        for (int i = 0; i < componentTypesInHashOrder.length ; i++)
        {
            Class<? extends Component> cls = componentTypesInHashOrder[i];
            ConcurrentMap<Entity, Component> map = componentStore[i];

            if (map == null)
            {
                componentMapSizes.put(cls.getName(), singletonConnections[i] != null ? 1 : 0);
            }
            else
            {
                componentMapSizes.put(cls.getName(), map.size());
            }

        }

        return new Status(entityMapSize, componentMapSizes);
    }

    EntitySystemInterface getInterface()
    {
        return new EntitySystemInterface(this);
    }

    //// ENTITY METHODS //////////////////////////////////////////////////

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
        // no assert since its used by other assert error reporting
        return entitiesToNames.get(entity);
    }

    public Set<Entity> entities()
    {
        return entitySetRO;
    }

    public void killEntity(Entity entity)
    {
        String name = entitiesToNames.remove(entity);
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert name != null : "Entity " + entity + " not found.";

        entity.setAlive(false);

        for (int i = 0; i < numberOfComponentTypes; i++)
        {
            ConcurrentMap<Entity, Component> map = componentStore[i];
            if (map == null)
            {
                SingletonComponentConnection connection = singletonConnections[i];
                if (connection != null && connection.entity.getId() == entity.getId())
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
    public void setEntityName(Entity entity, String name)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        if (name == null)
        {
            name = UNNAMED;
        }

        String oldName = entitiesToNames.replace(entity, name);

        // there are no null names in our map, so a null means "No entity" here.
        assert oldName != null : "Entity " + entity + " does not exist.";
    }

    private Entity createAndRegisterEntity(String name)
    {
        Entity entity = new Entity(idGenerator.getNextEntityId());
        entitiesToNames.put(entity, name);
        return entity;
    }


    //// COMPONENT METHODS ///////////////////////////////////////////////

    public <T extends Component> void addComponent(Entity entity, T component)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        Class<? extends Component> componentType = component.getClass();
        int index = getTypeIndex(componentType);

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            singletonConnections[index] = new SingletonComponentConnection((SingletonComponent) component, entity);
        }
        else
        {
            componentStore[index].put(entity, component);
        }
    }

    public <T extends Component> T getComponent(Entity entity, Class<T> componentType)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        int index = getTypeIndex(componentType);
        return getComponentInternal(entity, index);
    }

    public <T extends Component> boolean hasComponent(Entity entity,
                                                      Class<T> componentType)
    {
        return getComponent(entity, componentType) != null;
    }

    public <T extends Component> void removeComponent(Entity entity, Class<T> componentType)
    {
        assert entity.isAlive() : "Entity " + nameFor(entity) + " is dead.";
        assert entitiesToNames.containsKey(entity) : "Entity " + entity + " not found.";

        int index = getTypeIndex(componentType);

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            SingletonComponentConnection connection = singletonConnections[index];
            if (connection != null && connection.entity.getId() == entity.getId())
            {
                singletonConnections[index] = null;
            }
        }
        else
        {
            componentStore[index].remove(entity);
        }
    }

    /**
     * Returns the component for the given entity should such a component exist. Internal
     * method that does not validate entity identity or lifecycle.
     */
    <T extends Component> T getComponentInternal(Entity entity, int componentTypeIndex)
    {
        ConcurrentMap<Entity, Component> map = componentStore[componentTypeIndex];
        if (map == null)
        {
            SingletonComponentConnection connection = singletonConnections[componentTypeIndex];
            if (connection != null && connection.entity.getId() == entity.getId())
            {
                return (T) connection.component;
            }

            return (T) null;
        }
        else
        {
            return (T) map.get(entity);
        }
    }

    private Class<? extends Component>[] getSortedComponentTypes(
        Set<Class<? extends Component>> componentClasses)
    {
        Map<Integer, Class<? extends Component>> knownHashes = new HashMap<Integer, Class<? extends Component>>();
        Class<? extends Component>[] componentTypesInHashOrder = new Class[numberOfComponentTypes];
        int index = 0;
        for (Class<? extends Component> componentClass : componentClasses)
        {
            int componentHash = componentClass.hashCode();

            Class<? extends Component> otherComponentClass = knownHashes.get(componentHash);
            if (otherComponentClass != null)
            {
                throw new ComponentHashNotUniqueException("The component " + componentClass + " and " +
                    otherComponentClass + " have the same hashCode. " +
                    "This unfortunately violates one of our requirements. I'm afraid I have to ask you to change " +
                    "*something* on these classes.");
            }
            componentTypesInHashOrder[index++] = componentClass;
            knownHashes.put(componentHash, componentClass);
        }

        Arrays.sort(componentTypesInHashOrder, HashOrderComparator.INSTANCE);

        return componentTypesInHashOrder;
    }

    int getTypeIndex(Class<? extends Component> componentClass)
    {
        int index = Arrays.binarySearch(componentTypesInHashOrder, componentClass, HashOrderComparator.INSTANCE);
        if (index < 0)
        {
            throw new InvalidComponentTypeException(componentClass + " is not known by this system");
        }
        return index;
    }

    //// QUERY METHODS ///////////////////////////////////////////////////

    /**
     * Slow convenience methods to get all components on a certain entity.
     * <p/>
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

        for (int i = 0; i < numberOfComponentTypes; i++)
        {
            Component c = getComponentInternal(entity, i);

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
            return (Collection<T>) componentValuesRO[index];
        }
    }

    public <T extends Component> Map<Entity, T> getEntityToComponentMap(
        Class<T> componentType)
    {
        int index = getTypeIndex(componentType);

        if (SingletonComponent.class.isAssignableFrom(componentType))
        {
            throw new UnsupportedOperationException("Singletons not supported in getEntityToComponentMap");
        }
        else
        {
            return (Map<Entity, T>) componentStoreRO[index];
        }
    }

    public Entity findEntityWithSingleton(Class<? extends SingletonComponent> componentType)
    {
        int index = getTypeIndex(componentType);
        SingletonComponentConnection connection = singletonConnections[index];
        return (Entity) (connection != null ? connection.entity : null);
    }

    public <T extends SingletonComponent> T findSingletonComponent(Class<T> componentType)
    {
        int index = getTypeIndex(componentType);
        SingletonComponentConnection connection = singletonConnections[index];
        return (T) ( connection != null ?  connection.component : null);
    }

    public Set<Entity> findEntitiesWithComponent(
        Class<? extends Component> componentType)
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

    //// INTERNAL CLASSES ////////////////////////////////////////////////

    /**
     * Immutable class to encapsulate the attribution of a singleton component to an entity.
     * <p/>
     * Offers two set views on the component and entity via lazy initialized {@link org.javesi.util.SingletonSet] impl.
     */
    private static class SingletonComponentConnection
    {
        public final SingletonComponent component;
        public final Entity entity;
        private SingletonSet<Component> componentSet;
        private SingletonSet<Entity> entitySet;

        public SingletonComponentConnection(SingletonComponent component, Entity entity)
        {
            assert entity != null : "Entity can't be null";
            assert component != null : "Component can't be null";

            this.component = component;
            this.entity = entity;
        }

        public Set<Component> components()
        {
            SingletonSet<Component> set = componentSet;

            if (set == null)
            {
                set = new SingletonSet<Component>(this.component);
                componentSet = set;
            }
            return set;
        }

        public Set<Entity> entities()
        {
            SingletonSet<Entity> set = entitySet;

            if (set == null)
            {
                set = new SingletonSet<Entity>(this.entity);
                entitySet = set;
            }

            return set;
        }
    }

    //// STATUS /////////////////////////////////////////////////////////

    public static final class Status
    {
        private final int entityMapSize;

        private final Map<String,Integer> componentMapSizes;
        private final long created;

        Status(int entityMapSize, Map<String, Integer> componentMapSizes)
        {
            this.entityMapSize = entityMapSize;
            this.componentMapSizes = componentMapSizes;
            this.created = System.currentTimeMillis();
        }

        public int getEntityMapSize()
        {
            return entityMapSize;
        }

        public Map<String, Integer> getComponentMapSizes()
        {
            return componentMapSizes;
        }


        public long getCreated()
        {
            return created;
        }

        private final static String COLUMN = "                                        ";

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("-- Entity system status (created ");
            sb.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(created)));
            sb.append("):").append(SEP);
            sb.append("Entity map size : ").append(entityMapSize).append(", ");

            sb.append("component map sizes :").append(SEP);
            for (Map.Entry<String,Integer> e : componentMapSizes.entrySet())
            {
                String key = e.getKey();

                if (key.length() > COLUMN.length())
                {
                    sb.append(key.substring(key.length() - COLUMN.length()));
                }
                else
                {
                    sb.append(key).append(COLUMN.substring(key.length()));
                }


                sb.append(" = ").append(e.getValue()).append(SEP);
            }

            return sb.toString();
        }
    }


}
