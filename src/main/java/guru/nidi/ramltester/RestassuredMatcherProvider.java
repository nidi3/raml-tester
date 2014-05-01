package guru.nidi.ramltester;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.module.jsv.JsonSchemaValidatorSettings;
import org.hamcrest.Matcher;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

/**
 *
 */
public class RestassuredMatcherProvider implements MatcherProvider<String> {
    private final JsonSchemaFactory schemaFactory;
    private final JsonSchemaValidatorSettings schemaValidatorSettings;

    private RestassuredMatcherProvider(JsonSchemaFactory schemaFactory, JsonSchemaValidatorSettings schemaValidatorSettings) {
        this.schemaFactory = schemaFactory;
        this.schemaValidatorSettings = schemaValidatorSettings;
    }

    public RestassuredMatcherProvider() {
        this(null, null);
    }

    public RestassuredMatcherProvider using(JsonSchemaFactory jsonSchemaFactory) {
        return new RestassuredMatcherProvider(jsonSchemaFactory, null);
    }

    public RestassuredMatcherProvider using(JsonSchemaValidatorSettings jsonSchemaValidatorSettings) {
        return new RestassuredMatcherProvider(null, jsonSchemaValidatorSettings);
    }

    @Override
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

}
