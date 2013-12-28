package org.javesi.test;

import org.javesi.EntitySystem;
import org.javesi.Job;
import org.javesi.exception.JavesyRuntimeException;
import org.junit.runners.model.Statement;

class TestJob
    implements Job
{
    private final Statement statement;
    private final TestEntitySystem rule;

    public TestJob(TestEntitySystem testEntitySystem, Statement base)
    {
        this.rule = testEntitySystem;
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
