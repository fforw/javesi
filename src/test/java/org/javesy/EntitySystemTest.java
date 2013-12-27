package org.javesy;

import org.javesy.testcomponents.ComponentA;
import org.javesy.testcomponents.SingleB;
import org.javesy.testcomponents.ComponentC;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EntitySystemTest
{
    @Test
    public void testEntitySystem()
    {
        EntitySystem system = createTestSystem();

        assertThat(system.entities().size(), is(0));

        Entity a = system.createNamedEntity("Entity A");
        Entity b = system.createNamedEntity("Entity B");
        Entity c = system.createNamedEntity("Entity C");
        Entity d = system.createNamedEntity("Entity D");

        assertThat(system.entities().size(), is(4));

        system.addComponent(a, createA("test 123"));
        system.addComponent(a, createB(30894));

        system.addComponent(c, createA("foo"));

        assertThat(system.hasComponent(a,ComponentA.class), is(true));
        assertThat(system.hasComponent(a,SingleB.class), is(true));
        assertThat(system.hasComponent(a,ComponentC.class), is(false));

        assertThat(system.hasComponent(b,ComponentA.class), is(false));
        assertThat(system.hasComponent(b,SingleB.class), is(false));
        assertThat(system.hasComponent(b,ComponentC.class), is(false));

        assertThat(system.hasComponent(c,ComponentA.class), is(true));
        assertThat(system.hasComponent(c,SingleB.class), is(false));
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
        assertThat(entitiesWithB.size(),is(1));
        assertThat(entitiesWithB.contains(a),is(true));

        Set<Entity> entitiesWithA = system.findEntitiesWithComponent(ComponentA.class);
        assertThat(entitiesWithA.size(),is(2));
        assertThat(entitiesWithA.contains(a),is(true));
        assertThat(entitiesWithA.contains(c),is(true));

        Set<Entity> entitiesWithAandB = system.findEntitiesWithComponents(ComponentA.class, SingleB.class);
        assertThat(entitiesWithB.size(),is(1));
        assertThat(entitiesWithB.contains(a),is(true));

        // kill entity
        system.killEntity(c);

        assertThat(system.entities().contains(c), is(false) );
        assertThat(system.getComponentInternal(c, ComponentA.class), is(nullValue()));

        Set<Entity> entitiesWithAAfterRemoval = system.findEntitiesWithComponent(ComponentA.class);
        assertThat(entitiesWithAAfterRemoval.size(), is(1));
        assertThat(entitiesWithAAfterRemoval.contains(a), is(true));

        // add component c
        system.addComponent(b,new ComponentC());
        assertThat(system.hasComponent(b, ComponentC.class), is(true));

        // remove component c
        system.removeComponent(b, ComponentC.class);
        assertThat(system.hasComponent(b, ComponentC.class), is(false));

        // move singleton to D
        assertThat(system.hasComponent(a, SingleB.class), is(true));
        system.addComponent(d,createB(12));
        assertThat(system.hasComponent(d, SingleB.class), is(true));
        assertThat(system.hasComponent(a, SingleB.class), is(false));

        entitiesWithB = system.findEntitiesWithComponent(SingleB.class);
        assertThat(entitiesWithB.size(),is(1));
        assertThat(entitiesWithB.contains(d),is(true));

    }

    private EntitySystem createTestSystem()
    {
        Set<Class<? extends Component>> set = new HashSet<Class<? extends Component>>();
        set.add(ComponentA.class);
        set.add(SingleB.class);
        set.add(ComponentC.class);

        return new EntitySystemBuilder().buildFromComponentClasses(set);
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

    private Entity NOT_EXISTING_IN_SYSTEM = new Entity(42l);

    @Test(expected =  AssertionError.class)
    public void thatInvalidKillsAreDetected()
    {
        createTestSystem().killEntity(NOT_EXISTING_IN_SYSTEM);
    }

    @Test(expected =  AssertionError.class)
    public void thatInvalidGetNameIsDetected()
    {
        createTestSystem().nameFor(NOT_EXISTING_IN_SYSTEM);
    }
    
    @Test(expected =  AssertionError.class)
    public void thatInvalidComponentRemovesAreDetected()
    {
        createTestSystem().removeComponent(NOT_EXISTING_IN_SYSTEM, ComponentA.class);
    }

    @Test(expected =  AssertionError.class)
    public void thatInvalidHasComponentIsDetected()
    {
        createTestSystem().hasComponent(NOT_EXISTING_IN_SYSTEM, ComponentA.class);
    }

    @Test(expected =  AssertionError.class)
    public void thatInvalidGetComponentIsDetected()
    {
        createTestSystem().getComponent(NOT_EXISTING_IN_SYSTEM, ComponentA.class);
    }

    @Test(expected =  AssertionError.class)
    public void thatInvalidAddComponentIsDetected()
    {
        createTestSystem().addComponent(NOT_EXISTING_IN_SYSTEM, new ComponentA());
    }

    @Test(expected =  AssertionError.class)
    public void thatInvalidGetAllComponentsIsDetected()
    {
        createTestSystem().getAllComponentsOnEntity(NOT_EXISTING_IN_SYSTEM);
    }
}
