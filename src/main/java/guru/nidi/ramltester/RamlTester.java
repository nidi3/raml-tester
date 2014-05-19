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

import guru.nidi.ramltester.apidesigner.ApiRamlLoader;
import guru.nidi.ramltester.loader.ClassPathRamlLoader;
import guru.nidi.ramltester.loader.FileRamlLoader;
import guru.nidi.ramltester.loader.RamlLoader;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class RamlTester {
    private RamlTester() {
    }

    public static RamlLoaders fromClasspath(Class<?> basePackage) {
        return fromClasspath(basePackage.getPackage().getName().replace('.', '/'));
    }

    public static RamlLoaders fromClasspath(String basePackage) {
        return usingLoader(new ClassPathRamlLoader(basePackage));
    }

    public static RamlLoaders fromFile(File baseDirectory) {
        return usingLoader(new FileRamlLoader(baseDirectory));
    }

    public static RamlLoaders fromApiPortal(String user, String password) throws IOException {
        return usingLoader(new ApiRamlLoader(user, password));
    }

    public static RamlLoaders fromApiDesigner(String url) throws IOException {
        return usingLoader(new ApiRamlLoader(url));
    }

    public static RamlLoaders usingLoader(RamlLoader loader) {
        return new RamlLoaders(loader);
    }
}
