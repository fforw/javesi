package org.javesy;

import org.javesy.testcomponents.ComponentA;
import org.javesy.testcomponents.SingleB;
import org.javesy.testcomponents.ComponentC;
import org.javesy.testcomponents.UnusedSingle;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Set;

public class SystemBuilderTest
{
    @Test
    public void thatPackageScanningWorks()
    {
        Set<Class<? extends Component>> componentClasses = new EntitySystemBuilder().findComponentClasses("org.javesy" +
            ".testcomponents");

        assertThat(componentClasses.size(), is(4));
        assertThat(componentClasses.contains(ComponentA.class), is(true));
        assertThat(componentClasses.contains(SingleB.class), is(true));
        assertThat(componentClasses.contains(ComponentC.class), is(true));
        assertThat(componentClasses.contains(UnusedSingle.class), is(true));
    }

}
