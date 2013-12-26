package org.javesy;

import org.javesy.testcomponents.ComponentA;
import org.javesy.testcomponents.ComponentB;
import org.javesy.testcomponents.ComponentC;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Set;

public class SystemBuilderTest
{
    @Test
    public void thatPackageScanningWorks()
    {

        Set<Class<? extends Component>> componentClasses = new SystemBuilder().findComponentClasses("org.javesy" +
            ".testcomponents");

        assertThat(componentClasses.size(), is(3));
        assertThat(componentClasses.contains(ComponentA.class), is(true));
        assertThat(componentClasses.contains(ComponentB.class), is(true));
        assertThat(componentClasses.contains(ComponentC.class), is(true));
    }

}
