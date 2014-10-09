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
package guru.nidi.ramltester.loader;

/**
 *
 */
public class DefaultRamlLoaderFactories {
    public static class ClassPath implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "classpath";
        }

        @Override
        public RamlLoader getRamlLoader(String base) {
            return new ClassPathRamlLoader(base);
        }
    }

    public static class File implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "file";
        }

        @Override
        public RamlLoader getRamlLoader(String base) {
            return new FileRamlLoader(new java.io.File(base));
        }
    }

    public static class Http implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "http";
        }

        @Override
        public RamlLoader getRamlLoader(String base) {
            return new UrlRamlLoader("http://" + base);
        }
    }

    public static class Https implements RamlLoaderFactory {
        @Override
        public String supportedProtocol() {
            return "https";
        }

        @Override
        public RamlLoader getRamlLoader(String base) {
            return new UrlRamlLoader("https://" + base);
        }
    }
}
