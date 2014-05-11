package guru.nidi.ramltester.core;

import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.module.jsv.JsonSchemaValidatorSettings;
import guru.nidi.ramltester.loader.RamlResourceLoader;
import guru.nidi.ramltester.loader.UriDownloaderResourceLoader;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

/**
 *
 */
public class RestassuredSchemaValidator implements SchemaValidator {
    private final JsonSchemaFactory schemaFactory;
    private final JsonSchemaValidatorSettings schemaValidatorSettings;

    private RestassuredSchemaValidator(JsonSchemaFactory schemaFactory, JsonSchemaValidatorSettings schemaValidatorSettings) {
        this.schemaFactory = schemaFactory;
        this.schemaValidatorSettings = schemaValidatorSettings;
    }

    public RestassuredSchemaValidator() {
        this(null, null);
    }

    public RestassuredSchemaValidator using(JsonSchemaFactory jsonSchemaFactory) {
        return new RestassuredSchemaValidator(jsonSchemaFactory, null);
    }

    public RestassuredSchemaValidator using(JsonSchemaValidatorSettings jsonSchemaValidatorSettings) {
        return new RestassuredSchemaValidator(null, jsonSchemaValidatorSettings);
    }

    @Override
    public SchemaValidator withResourceLoader(String base, RamlResourceLoader resourceLoader) {
        final LoadingConfigurationBuilder loadingConfig = LoadingConfiguration.newBuilder();
        final String namespace;
        if (resourceLoader != null) {
            namespace = base + ":///";
            loadingConfig.addScheme(base, new UriDownloaderResourceLoader(resourceLoader));
        } else {
            namespace = base.endsWith("/") ? base : (base + "/");
        }
        loadingConfig.setURITranslatorConfiguration(URITranslatorConfiguration.newBuilder().setNamespace(namespace).freeze());
        return using(JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfig.freeze()).freeze());
    }

    @SuppressWarnings("unchecked")
    public Matcher<String> getMatcher(String data) {
        if (schemaFactory != null) {
            return (Matcher<String>) matchesJsonSchema(data).using(schemaFactory);
        }
        if (schemaValidatorSettings != null) {
            return (Matcher<String>) matchesJsonSchema(data).using(schemaValidatorSettings);
        }
        return matchesJsonSchema(data);
    }

    @Override
    public void validate(String content, String schema, RamlViolations violations, Message message) {
        final Matcher<String> matcher = getMatcher(schema);
        if (!matcher.matches(content)) {
            Description description = new StringDescription().appendDescriptionOf(matcher);
            violations.add(message.withParam(content).withParam(description.toString()));
        }
    }
}
