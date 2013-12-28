package org.javesy.test;

import org.javesy.EntitySystem;
import org.javesy.Job;
import org.javesy.exception.JavesyRuntimeException;
import org.junit.runners.model.Statement;

class TestJob
    implements Job
{
    private final Statement statement;
    private final EntitySystemRule rule;

    public TestJob(EntitySystemRule entitySystemRule, Statement base)
    {
        this.rule = entitySystemRule;
        this.statement = base;
    }

    @Override
    public void execute(EntitySystem state)
    {
        try
        {
            rule.setEntitySystem(state);
            statement.evaluate();
        }
        catch (Throwable throwable)
        {
            throw new JavesyRuntimeException(throwable);
        }
        finally
        {
            rule.setEntitySystem(null);
        }
    }
}
