package org.javesi;

import org.javesi.id.EntityIdGenerator;
import org.javesi.testcomponents.ComponentA;
import org.javesi.testcomponents.SingleB;
import org.javesi.testcomponents.ComponentC;
import org.javesi.testcomponents.UnusedSingle;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SystemBuilderTest
{
    private static Logger log = LoggerFactory.getLogger(SystemBuilderTest.class);

    @Test
    public void thatPackageScanningWorks()
    {
        Set<Class<? extends Component>> componentClasses =
            new EntitySystemBuilder().findComponentClasses("org.javesi.testcomponents");

        assertThat(componentClasses.size(), is(4));
        assertThat(componentClasses.contains(ComponentA.class), is(true));
        assertThat(componentClasses.contains(SingleB.class), is(true));
        assertThat(componentClasses.contains(ComponentC.class), is(true));
        assertThat(componentClasses.contains(UnusedSingle.class), is(true));
    }
    
    @Test
    public void checkBuilderContract() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // for every private field in the builder there has to be a "with"-er that returns the builder and a getter
        // that returns the value
        
        Set<String> properties = findEntitySystemBuilderProperties();

        System.out.println("Checking contract on " + properties);

        EntitySystemBuilder builder = new EntitySystemBuilder();

        for (String nameSuffix : properties)
        {
            Method getter = EntitySystemBuilder.class.getMethod("get" + nameSuffix, (Class[])null);
            Class<?> propertyType = getter.getReturnType();
            Method wither = EntitySystemBuilder.class.getMethod("with" + nameSuffix, new Class<?>[]{ propertyType } );

            Object value = random(propertyType);
            Object returned = wither.invoke(builder, new Object[] { value });

            assertThat("with" + nameSuffix + ": Return value is not the same builder", (EntitySystemBuilder) returned, is(builder));

            Object out = getter.invoke(builder, (Object[]) null);
            assertThat("get" + nameSuffix + ": 'With'ed value did not come out of get again", out, is(value));
            assertThat("EntitySystemConfig interface is missing get" + nameSuffix, EntitySystemConfig.class.getMethod(getter.getName(),(Class[])null), is(notNullValue()));
        }
    }

    private Random random = new Random();

    private Object random(Class<?> propertyType)
    {
        if (propertyType.equals(float.class))
        {
            return random.nextFloat();
        }
        else if (propertyType.equals(int.class))
        {
            return random.nextInt();
        }
        else if (propertyType.equals(EntityIdGenerator.class))
        {
            return new ConstantIdGenerator(random.nextLong());
        }
        else if (propertyType.equals(Set.class))
        {
            Set<Integer> set = new HashSet<Integer>();
            set.add(random.nextInt());
            return set;
        }
        else
        {
            throw new UnsupportedOperationException(propertyType + " not yet supported by test. Implement it.");
        }
    }

    private Set<String> findEntitySystemBuilderProperties()
    {
        Set<String> set = new HashSet<String>();
        for (Field f : EntitySystemBuilder.class.getDeclaredFields())
        {
            String name = f.getName();
            name = name.substring(0,1).toUpperCase() + name.substring(1);
            set.add(name);
        }

        return set;
    }

}
