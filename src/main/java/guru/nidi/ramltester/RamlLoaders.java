/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester;

import guru.nidi.loader.Loader;
import guru.nidi.loader.apidesigner.ApiLoader;
import guru.nidi.loader.basic.*;
import guru.nidi.loader.url.GithubLoader;
import guru.nidi.loader.url.UrlLoader;
import guru.nidi.loader.use.raml.LoaderRamlResourceLoader;
import guru.nidi.loader.use.raml.RamlCache;
import guru.nidi.ramltester.core.*;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;

import java.io.File;

public class RamlLoaders {
    private final Loader loader;
    private final SchemaValidators schemaValidators;
    private final boolean caching;

    public RamlLoaders(Loader loader, SchemaValidators schemaValidators, boolean caching) {
        this.loader = loader;
        this.schemaValidators = schemaValidators;
        this.caching = caching;
    }

    private static Loader classpathLoader(Class<?> basePackage) {
        return classpathLoader(basePackage.getPackage().getName().replace('.', '/'));
    }

    private static Loader classpathLoader(String basePackage) {
        return new ClassPathLoader(basePackage);
    }

    private static Loader fileLoader(File baseDirectory) {
        return new FileLoader(baseDirectory);
    }

    private static Loader fileLoader(String baseDirectory) {
        return new FileLoader(new File(baseDirectory));
    }

    private static Loader urlLoader(String baseUrl) {
        return new UrlLoader(baseUrl);
    }

    private static Loader githubLoader(String token, String user, String project, String ref) {
        final int pos = project.indexOf('/');
        final GithubLoader loader = pos < 0
                ? GithubLoader.forPrivate(token, user, project)
                : GithubLoader.forPrivate(token, user, project.substring(0, pos)).resourceBase(project.substring(pos + 1));
        return loader.ref(ref);
    }

    private static Loader apiPortalLoader(String user, String password) {
        return new ApiLoader(user, password);
    }

    private static Loader apiDesignerLoader(String url) {
        return new ApiLoader(url);
    }

    public static RamlLoaders fromClasspath(Class<?> basePackage) {
        return using(classpathLoader(basePackage));
    }

    public static RamlLoaders fromClasspath(String basePackage) {
        return using(classpathLoader(basePackage));
    }

    public static RamlLoaders fromClasspath() {
        return using(classpathLoader(""));
    }

    public static RamlLoaders fromFile(File baseDirectory) {
        return using(fileLoader(baseDirectory));
    }

    public static RamlLoaders fromFile(String baseDirectory) {
        return using(fileLoader(baseDirectory));
    }

    public static RamlLoaders fromUrl(String baseUrl) {
        return using(urlLoader(baseUrl));
    }

    public static RamlLoaders fromGithub(String user, String project) {
        return fromGithub(null, user, project, null);
    }

    public static RamlLoaders fromGithub(String token, String user, String project, String ref) {
        return using(githubLoader(token, user, project, ref));
    }

    public static RamlLoaders fromApiPortal(String user, String password) {
        return using(apiPortalLoader(user, password));
    }

    public static RamlLoaders fromApiDesigner(String url) {
        return using(apiDesignerLoader(url));
    }

    /**
     * These URI schemas are supported:
     * <pre>
     * - http://google.com/file.raml
     * - user:password@https://google.com/file.raml
     * - file:///tmp/temp.raml
     * - classpath://guru/nidi/ramltester/simple.raml
     * - token@github://nidi3/raml-tester/src/test/resources/simple.raml
     * - user:password@apiportal://test.raml
     * </pre>
     * TODO - apidesigner://
     *
     * @return
     */
    public static RamlLoaders absolutely() {
        return using(null);
    }

    public static RamlLoaders using(Loader loader) {
        return new RamlLoaders(loader, SchemaValidators.standard(), false);
    }


    public RamlLoaders andFromClasspath(Class<?> basePackage) {
        return andUsing(classpathLoader(basePackage));
    }

    public RamlLoaders andFromClasspath(String basePackage) {
        return andUsing(classpathLoader(basePackage));
    }

    public RamlLoaders andFromFile(File baseDirectory) {
        return andUsing(fileLoader(baseDirectory));
    }

    public RamlLoaders andFromFile(String baseDirectory) {
        return andUsing(fileLoader(baseDirectory));
    }

    public RamlLoaders andFromUrl(String baseUrl) {
        return andUsing(urlLoader(baseUrl));
    }

    public RamlLoaders andFromGithub(String user, String project) {
        return andFromGithub(null, user, project, null);
    }

    public RamlLoaders andFromGithub(String token, String user, String project, String ref) {
        return andUsing(githubLoader(token, user, project, ref));
    }

    public RamlLoaders andFromApiPortal(String user, String password) {
        return andUsing(apiPortalLoader(user, password));
    }

    public RamlLoaders andFromApiDesigner(String url) {
        return andUsing(apiDesignerLoader(url));
    }

    public RamlLoaders andUsing(Loader loader) {
        return new RamlLoaders(new CompositeLoader(this.loader, loader), schemaValidators, caching);
    }

    public RamlLoaders addSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlLoaders(loader, schemaValidators.addSchemaValidator(schemaValidator), caching);
    }

    public RamlDefinition load(String name) {
        final Loader decorated = new UriLoader(loader);
        final RamlModelResult raml = caching
                ? new RamlCache(decorated).loadRaml(name)
                //                : new RelativeJsonSchemaAwareRamlDocumentBuilder(decorated,
                // new LoaderRamlResourceLoader(decorated)).build(name);
                : new RamlModelBuilder(new LoaderRamlResourceLoader(decorated)).buildApi(name);
        if (raml.hasErrors()) {
            throw new RamlViolationException(RamlReport.fromModelResult(null, raml));
        }
        final SchemaValidators validators = schemaValidators.withloader(decorated);
        return new RamlDefinition(raml, validators);
    }
}

