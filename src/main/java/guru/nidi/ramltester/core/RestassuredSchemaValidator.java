package guru.nidi.ramltester.core;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.module.jsv.JsonSchemaValidatorSettings;
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
    public void validate(RamlViolations violations, String content, String schema) {
        final Matcher<String> matcher = getMatcher(schema);
        if (!matcher.matches(content)) {
            Description description = new StringDescription();
            description.appendText("HttpResponse content ");
            description.appendValue(content);
            description.appendText(" does not match schema: ");
            description.appendDescriptionOf(matcher);
            violations.add(description.toString());
        }
    }
}
