package org.javesi;

import org.javesi.exception.InvalidComponentTypeException;
import org.javesi.test.EntitySystemRule;
import org.javesi.testcomponents.ComponentA;
import org.javesi.testcomponents.SingleB;
import org.javesi.testcomponents.ComponentC;
import org.javesi.testcomponents.UnusedSingle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EntitySystemTest
{
    private static Logger log = LoggerFactory.getLogger(EntitySystemTest.class);
    @Rule
    public EntitySystemRule rule = new EntitySystemRule(ComponentA.class, ComponentC.class, SingleB.class,
        UnusedSingle.class);
    @Rule
    public EntitySystemRule common = new EntitySystemRule(ComponentA.class, ComponentC.class, SingleB.class,
        UnusedSingle.class);
    private Entity NOT_EXISTING_IN_SYSTEM = new Entity(42l);

    {
        NOT_EXISTING_IN_SYSTEM.setAlive(false);
    }

    @Test
    public void testEntitySystem()
    {
        EntitySystem system = rule.getEntitySystem();

        assertThat(system.entities().size(), is(0));

        Entity a = system.createNamedEntity("Entity A");
        Entity b = system.createNamedEntity("Entity B");
        Entity c = system.createNamedEntity("Entity C");
        Entity d = system.createNamedEntity("Entity D");

        assertThat(system.entities().size(), is(4));

        system.addComponent(a, createA("test 123"));
        system.addComponent(a, createB(30894));

        system.addComponent(c, createA("foo"));

        assertThat(system.hasComponent(a, ComponentA.class), is(true));
        assertThat(system.hasComponent(a, SingleB.class), is(true));
        assertThat(system.hasComponent(a, ComponentC.class), is(false));

        assertThat(system.hasComponent(b, ComponentA.class), is(false));
        assertThat(system.hasComponent(b, SingleB.class), is(false));
        assertThat(system.hasComponent(b, ComponentC.class), is(false));

        assertThat(system.hasComponent(c, ComponentA.class), is(true));
        assertThat(system.hasComponent(c, SingleB.class), is(false));
        assertThat(system.hasComponent(c, ComponentC.class), is(false));

        // check name
        assertThat(system.nameFor(a), is("Entity A"));
        assertThat(system.nameFor(b), is("Entity B"));
        assertThat(system.nameFor(c), is("Entity C"));
        assertThat(system.nameFor(d), is("Entity D"));


        Collection<ComponentA> compAs = system.getAllComponentsOfType(ComponentA.class);
        assertThat(compAs.size(), is(2));

        Collection<SingleB> compBs = system.getAllComponentsOfType(SingleB.class);
        assertThat(compBs.size(), is(1));

        Collection<ComponentC> compCs = system.getAllComponentsOfType(ComponentC.class);
        assertThat(compCs.size(), is(0));

        Set<Entity> entitiesWithB = system.findEntitiesWithComponent(SingleB.class);
        assertThat(entitiesWithB.size(), is(1));
        assertThat(entitiesWithB.contains(a), is(true));

        Set<Entity> entitiesWithA = system.findEntitiesWithComponent(ComponentA.class);
        assertThat(entitiesWithA.size(), is(2));
        assertThat(entitiesWithA.contains(a), is(true));
        assertThat(entitiesWithA.contains(c), is(true));

        Set<Entity> entitiesWithAandB = system.findEntitiesWithComponents(ComponentA.class, SingleB.class);
        assertThat(entitiesWithB.size(), is(1));
        assertThat(entitiesWithB.contains(a), is(true));

        // kill entity
        system.killEntity(c);

        assertThat(system.entities().contains(c), is(false));
        int index = system.getTypeIndex(ComponentA.class);
        assertThat(system.getComponentInternal(c, index), is(nullValue()));

        Set<Entity> entitiesWithAAfterRemoval = system.findEntitiesWithComponent(ComponentA.class);
        assertThat(entitiesWithAAfterRemoval.size(), is(1));
        assertThat(entitiesWithAAfterRemoval.contains(a), is(true));

        // add component c
        system.addComponent(b, new ComponentC());
        assertThat(system.hasComponent(b, ComponentC.class), is(true));

        // remove component c
        system.removeComponent(b, ComponentC.class);
        assertThat(system.hasComponent(b, ComponentC.class), is(false));

        // move singleton to D
        assertThat(system.hasComponent(a, SingleB.class), is(true));
        system.addComponent(d, createB(12));
        assertThat(system.hasComponent(d, SingleB.class), is(true));
        assertThat(system.hasComponent(a, SingleB.class), is(false));

        entitiesWithB = system.findEntitiesWithComponent(SingleB.class);
        assertThat(entitiesWithB.size(), is(1));
        assertThat(entitiesWithB.contains(d), is(true));

        // removing an non-existing component executes but has no effect
        assertThat(system.hasComponent(b, ComponentC.class), is(false));
        system.removeComponent(b, ComponentC.class);
        assertThat(system.hasComponent(b, ComponentC.class), is(false));

        // removing an unused singleton component executes but has no effect
        assertThat(system.hasComponent(a, UnusedSingle.class), is(false));
        system.removeComponent(a, UnusedSingle.class);
        assertThat(system.hasComponent(a, UnusedSingle.class), is(false));

    }

    private ComponentA createA(String value)
    {
        ComponentA componentA = new ComponentA();
        componentA.value = value;
        return componentA;
    }

    private SingleB createB(int value)
    {
        SingleB componentB = new SingleB();
        componentB.value = value;
        return componentB;
    }

    private ComponentC createC()
    {
        return new ComponentC();
    }

    @Test(expected = AssertionError.class)
    public void thatInvalidKillsAreDetected()
    {
        try
        {
            EntitySystem state = common.getEntitySystem();
            state.killEntity(NOT_EXISTING_IN_SYSTEM);
        }
        catch (Exception e)
        {
            log.error("error killing entity", e);
        }
    }

    @Test(expected = AssertionError.class)
    public void thatInvalidComponentRemovesAreDetected()
    {
        try
        {
            EntitySystem state = common.getEntitySystem();
            state.removeComponent(NOT_EXISTING_IN_SYSTEM, ComponentA.class);
        }
        catch (Exception e)
        {
            log.error("error removing component", e);
        }
    }

    @Test(expected = AssertionError.class)
    public void thatInvalidHasComponentIsDetected()
    {
        try
        {
            EntitySystem state = common.getEntitySystem();
            state.hasComponent(NOT_EXISTING_IN_SYSTEM, ComponentA.class);
        }
        catch (Exception e)
        {
            log.error("error checking component", e);
        }
    }

    @Test(expected = AssertionError.class)
    public void thatInvalidGetComponentIsDetected()
    {
        try
        {
            EntitySystem state = common.getEntitySystem();
            state.getComponent(NOT_EXISTING_IN_SYSTEM, ComponentA.class);
        }
        catch (Exception e)
        {
            log.error("error getting component", e);
        }
    }

    @Test(expected = AssertionError.class)
    public void thatInvalidAddComponentIsDetected()
    {
        try
        {
            EntitySystem state = common.getEntitySystem();
            state.addComponent(NOT_EXISTING_IN_SYSTEM, new ComponentA());
        }
        catch (Exception e)
        {
            log.error("error adding component ", e);
        }
    }

    @Test(expected = AssertionError.class)
    public void thatInvalidGetAllComponentsIsDetected()
    {
        try
        {
            EntitySystem state = common.getEntitySystem();
            state.getAllComponentsOnEntity(NOT_EXISTING_IN_SYSTEM);
        }
        catch (Exception e)
        {
            log.error("error getting all components from entity", e);
        }
    }

    @Test(expected = InvalidComponentTypeException.class)
    public void thatUnknownComponentClassesAreDetected()
    {
        Set<Class<? extends Component>> set = new HashSet<Class<? extends Component>>();
        set.add(ComponentA.class);
        new EntitySystemBuilder().withComponentClasses(set).build().execute(new Job()
        {
            public void execute(EntitySystem state)
            {
                state.addComponent(state.createEntity(), new ComponentC());
            }
        });
    }
}
