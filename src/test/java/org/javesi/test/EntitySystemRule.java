package org.javesi.test;

import org.javesi.Component;
import org.javesi.EntitySystem;
import org.javesi.EntitySystemBuilder;
import org.javesi.EntitySystemInterface;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashSet;
import java.util.Set;

public class EntitySystemRule implements TestRule
{
    private EntitySystemInterface entitySystemInterface;
    private EntitySystem entitySystem;

    public EntitySystemRule(Class<? extends Component>... componentTypes)
    {
        Set<Class<? extends Component>> classes = new HashSet<Class<? extends Component>>();

        for (Class componentType : componentTypes)
        {
            classes.add(componentType);
        }

        entitySystemInterface = new EntitySystemBuilder().withComponentClasses(classes).build();
    }

    @Override
    public Statement apply(final Statement base, Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                entitySystemInterface.execute(new TestJob(EntitySystemRule.this, base));
            }
        };
    }

    void setEntitySystem(EntitySystem entitySystem)
    {
        this.entitySystem = entitySystem;
    }

    public EntitySystem getEntitySystem()
    {
        return entitySystem;
    }
}
