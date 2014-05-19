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

import guru.nidi.ramltester.apidesigner.ApiPortalLoader;
import guru.nidi.ramltester.loader.ClassPathRamlResourceLoader;
import guru.nidi.ramltester.loader.FileRamlResourceLoader;
import guru.nidi.ramltester.loader.RamlResourceLoader;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class TestRaml {
    private TestRaml() {
    }

    public static RamlLoader fromClasspath(Class<?> basePackage) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/'));
    }

    public static RamlLoader fromClasspath(String basePackage) {
        return usingLoader(new ClassPathRamlResourceLoader(basePackage));
    }

    public static RamlLoader fromFile(File baseDirectory) {
        return usingLoader(new FileRamlResourceLoader(baseDirectory));
    }

    public static RamlLoader fromApiPortal(String user, String password) throws IOException {
        return fromApiPortal(new ApiPortalLoader(user, password));
    }

    public static RamlLoader fromApiPortal(ApiPortalLoader loader) throws IOException {
        return usingLoader(loader);
    }

    public static RamlLoader usingLoader(RamlResourceLoader loader) {
        return new RamlLoader(loader);
    }
}
