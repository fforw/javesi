package org.javesy;

import org.javesy.testcomponents.ComponentA;
import org.javesy.testcomponents.ComponentB;
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

        int a = system.createNamedEntity("Entity A");
        int b = system.createNamedEntity("Entity B");
        int c = system.createNamedEntity("Entity C");

        assertThat(system.entities().size(), is(3));

        system.addComponent(a, createA("test 123"));
        system.addComponent(a, createB(30894));

        system.addComponent(c, createA("foo"));

        assertThat(system.hasComponent(a,ComponentA.class), is(true));
        assertThat(system.hasComponent(a,ComponentB.class), is(true));
        assertThat(system.hasComponent(a,ComponentC.class), is(false));

        assertThat(system.hasComponent(b,ComponentA.class), is(false));
        assertThat(system.hasComponent(b,ComponentB.class), is(false));
        assertThat(system.hasComponent(b,ComponentC.class), is(false));

        assertThat(system.hasComponent(c,ComponentA.class), is(true));
        assertThat(system.hasComponent(c,ComponentB.class), is(false));
        assertThat(system.hasComponent(c, ComponentC.class), is(false));

        // check name
        assertThat(system.nameFor(a), is("Entity A"));
        assertThat(system.nameFor(b), is("Entity B"));
        assertThat(system.nameFor(c), is("Entity C"));


        Collection<ComponentA> compAs = system.getAllComponentsOfType(ComponentA.class);
        assertThat(compAs.size(), is(2));

        Collection<ComponentB> compBs = system.getAllComponentsOfType(ComponentB.class);
        assertThat(compBs.size(), is(1));

        Collection<ComponentC> compCs = system.getAllComponentsOfType(ComponentC.class);
        assertThat(compCs.size(), is(0));

        Set<Integer> entitiesWithB = system.findEntitiesWithComponent(ComponentB.class);
        assertThat(entitiesWithB.size(),is(1));
        assertThat(entitiesWithB.contains(a),is(true));

        Set<Integer> entitiesWithA = system.findEntitiesWithComponent(ComponentA.class);
        assertThat(entitiesWithA.size(),is(2));
        assertThat(entitiesWithA.contains(a),is(true));
        assertThat(entitiesWithA.contains(c),is(true));

        Set<Integer> entitiesWithAandB = system.findEntitiesWithComponents(ComponentA.class, ComponentB.class);
        assertThat(entitiesWithB.size(),is(1));
        assertThat(entitiesWithB.contains(a),is(true));

        system.killEntity(c);

        assertThat(system.entities().contains(c), is(false) );
        assertThat(system.getComponentInternal(c,ComponentA.class), is(nullValue()));

        // kill entity

        Set<Integer> componentsWithAAfterRemoval = system.findEntitiesWithComponent(ComponentA.class);
        assertThat(componentsWithAAfterRemoval.size(), is(1));
        assertThat(componentsWithAAfterRemoval.contains(a), is(true));

        // remove component
        assertThat(system.hasComponent(a, ComponentB.class), is(true));
        system.removeComponent(a, ComponentB.class);
        assertThat(system.hasComponent(a, ComponentB.class), is(false));


    }

    private EntitySystem createTestSystem()
    {
        Set<Class<? extends Component>> set = new HashSet<Class<? extends Component>>();
        set.add(ComponentA.class);
        set.add(ComponentB.class);
        set.add(ComponentC.class);

        return EntitySystem.forComponentClasses(set);
    }

    private ComponentA createA(String value)
    {
        ComponentA componentA = new ComponentA();
        componentA.value = value;
        return componentA;
    }

    private ComponentB createB(int value)
    {
        ComponentB componentB = new ComponentB();
        componentB.value = value;
        return componentB;
    }


    private ComponentC createC()
    {
        return new ComponentC();
    }

    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidKillsAreDetected()
    {
        createTestSystem().killEntity(42);
    }

    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidGetNameIsDetected()
    {
        createTestSystem().nameFor(42);
    }
    
    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidComponentRemovesAreDetected()
    {
        createTestSystem().removeComponent(42, ComponentA.class);
    }

    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidHasComponentIsDetected()
    {
        createTestSystem().hasComponent(42, ComponentA.class);
    }

    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidGetComponentIsDetected()
    {
        createTestSystem().getComponent(42, ComponentA.class);
    }

    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidAddComponentIsDetected()
    {
        createTestSystem().addComponent(42, new ComponentA());
    }

    @Test(expected =  EntityNotFoundException.class)
    public void thatInvalidGetAllComponentsIsDetected()
    {
        createTestSystem().getAllComponentsOnEntity(42);
    }
}
