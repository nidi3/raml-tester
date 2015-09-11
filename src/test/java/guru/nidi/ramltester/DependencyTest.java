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

import static jdepend.framework.DependencyMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class DependencyTest {
    private static final String BASE = "guru.nidi.ramltester";
    private static JDepend depend;

    @BeforeClass
    public static void init() throws IOException {
        depend = new JDepend(new PackageFilter(Arrays.asList("org.", "java.", "com.", "javax.", "guru.nidi.loader")));
        depend.addDirectory("target/classes");
        depend.analyze();
    }

    @Test
    public void dependencies() {
        DependencyConstraint constraint = new DependencyConstraint();

        final JavaPackage
                base = constraint.addPackage(BASE),
                core = constraint.addPackage(BASE + ".core"),
                httpcomponents = constraint.addPackage(BASE + ".httpcomponents"),
                restassured = constraint.addPackage(BASE + ".restassured"),                 
                junit = constraint.addPackage(BASE + ".junit"),
                validator = constraint.addPackage(BASE + ".validator"),
                model = constraint.addPackage(BASE + ".model"),
                servlet = constraint.addPackage(BASE + ".servlet"),
                spring = constraint.addPackage(BASE + ".spring"),
                jaxrs = constraint.addPackage(BASE + ".jaxrs"),
                util = constraint.addPackage(BASE + ".util");

        base.dependsUpon(model);
        base.dependsUpon(core);
        base.dependsUpon(servlet);
        base.dependsUpon(httpcomponents);
        base.dependsUpon(restassured);
        base.dependsUpon(spring);
        base.dependsUpon(jaxrs);
        base.dependsUpon(validator);

        core.dependsUpon(model);
        core.dependsUpon(util);

        util.dependsUpon(model);

        servlet.dependsUpon(model);
        servlet.dependsUpon(util);
        servlet.dependsUpon(core);

        httpcomponents.dependsUpon(model);
        httpcomponents.dependsUpon(util);
        httpcomponents.dependsUpon(core);
        
        restassured.dependsUpon(model);
        restassured.dependsUpon(core);

        junit.dependsUpon(util);
        junit.dependsUpon(core);

        validator.dependsUpon(util);
        validator.dependsUpon(core);

        spring.dependsUpon(model);
        spring.dependsUpon(util);
        spring.dependsUpon(core);

        jaxrs.dependsUpon(model);
        jaxrs.dependsUpon(util);
        jaxrs.dependsUpon(core);

        
        assertThat(depend, matches(constraint));
    }

    @Test
    public void noCircularDependencies() {
        assertThat(depend, hasNoCycles());
    }

    @Test
    public void maxDistance() {
        System.out.println(distances(depend, "guru.nidi.ramltester"));
        assertThat(depend, hasMaxDistance("guru.nidi.ramltester", .9));
    }

}
