/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester;

import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import org.junit.BeforeClass;
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
    private static JDepend depend;

    @BeforeClass
    public static void init() throws IOException {
        depend = new JDepend(new PackageFilter(Arrays.asList("org.", "java.", "com.", "javax.")));
        depend.addDirectory("target/classes");
        depend.analyze();
    }

    @Test
    public void dependencies() throws IOException {
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
                jaxrs = constraint.addPackage(BASE + ".jaxrs"),
                util = constraint.addPackage(BASE + ".util");

        base.dependsUpon(model);
        base.dependsUpon(core);
        base.dependsUpon(servlet);
        base.dependsUpon(httpcomponents);
        base.dependsUpon(spring);
        base.dependsUpon(jaxrs);
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

        jaxrs.dependsUpon(model);
        jaxrs.dependsUpon(util);
        jaxrs.dependsUpon(core);

        assertTrue("Dependency mismatch", depend.dependencyMatch(constraint));
    }

    @Test
    public void noCircularDependencies() throws IOException {
        assertFalse("Cyclic dependencies", depend.containsCycles());
    }

    @Test
    public void maxDistance() throws IOException {
        @SuppressWarnings("unchecked")
        final Collection<JavaPackage> packages = depend.getPackages();

        System.out.println("Name                                      abst  inst  dist");
        System.out.println("----------------------------------------------------------");
        for (JavaPackage pack : packages) {
            if (pack.getName().startsWith("guru.")) {
                System.out.printf("%-40s: %-1.2f  %-1.2f  %-1.2f%n", pack.getName(), pack.abstractness(), pack.instability(), pack.distance());
                assertEquals("Distance exceeded: " + pack.getName(), 0, pack.distance(), .86f);
            }
        }
    }

}
