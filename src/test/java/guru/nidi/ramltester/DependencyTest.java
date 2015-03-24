package guru.nidi.ramltester;

import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 *
 */
public class DependencyTest {
    private static final String BASE = "guru.nidi.ramltester";

    @Test
    public void dependencies() throws IOException {
        final JDepend jDepend = new JDepend(new PackageFilter(Arrays.asList("org.", "java.", "com.", "javax.")));
        jDepend.addDirectory("target/classes");

        DependencyConstraint constraint = new DependencyConstraint();

        final JavaPackage
                base = constraint.addPackage(BASE),
                apidesigner = constraint.addPackage(BASE + ".apidesigner"),
                core = constraint.addPackage(BASE + ".core"),
                httpcomponents = constraint.addPackage(BASE + ".httpcomponents"),
                junit = constraint.addPackage(BASE + ".junit"),
                loader = constraint.addPackage(BASE + ".loader"),
                model = constraint.addPackage(BASE + ".model"),
                servlet = constraint.addPackage(BASE + ".servlet"),
                spring = constraint.addPackage(BASE + ".spring"),
                util = constraint.addPackage(BASE + ".util");

        base.dependsUpon(model);
        base.dependsUpon(core);
        base.dependsUpon(servlet);
        base.dependsUpon(httpcomponents);
        base.dependsUpon(spring);
        base.dependsUpon(loader);
        base.dependsUpon(apidesigner);

        apidesigner.dependsUpon(loader);

        util.dependsUpon(model);

        servlet.dependsUpon(model);
        servlet.dependsUpon(util);
        servlet.dependsUpon(core);

        httpcomponents.dependsUpon(model);
        httpcomponents.dependsUpon(util);
        httpcomponents.dependsUpon(core);

        spring.dependsUpon(model);
        spring.dependsUpon(util);
        spring.dependsUpon(core);

        jDepend.analyze();

        assertTrue("Dependency mismatch", jDepend.dependencyMatch(constraint));
    }

    @Test
    public void circular() throws IOException {
        final JDepend jDepend = new JDepend();
        jDepend.addDirectory("target/classes");

        final Collection<JavaPackage> packages = jDepend.analyze();
        assertFalse("Cyclic dependencies", jDepend.containsCycles());

        System.out.println("Name                                      abst  inst  dist");
        System.out.println("----------------------------------------------------------");
        for (JavaPackage pack : packages) {
            if (pack.getName().startsWith("guru.")) {
                System.out.printf("%-40s: %-1.2f  %-1.2f  %-1.2f%n", pack.getName(), pack.abstractness(), pack.instability(), pack.distance());
                assertEquals("Distance exceeded: " + pack.getName(), 0, pack.distance(), .4f);
            }
        }
    }

}
