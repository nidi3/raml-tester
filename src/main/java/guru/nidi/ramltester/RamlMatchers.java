package guru.nidi.ramltester;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.module.jsv.JsonSchemaValidatorSettings;
import org.hamcrest.Matcher;
import org.raml.model.*;
import org.raml.model.parameter.QueryParameter;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

/**
 *
 */
public class RamlMatchers {
    public static RamlMatcher matchesRaml(String raml) {
        return new RamlMatcher(raml);
    }

    public static class RamlMatcher implements ResultMatcher {
        private final Raml raml;
        private final JsonSchemaFactory schemaFactory;
        private final JsonSchemaValidatorSettings schemaValidatorSettings;

        private RamlMatcher(Raml raml, JsonSchemaFactory schemaFactory, JsonSchemaValidatorSettings schemaValidatorSettings) {
            this.raml = raml;
            this.schemaFactory = schemaFactory;
            this.schemaValidatorSettings = schemaValidatorSettings;
        }

        private RamlMatcher(String raml) {
            this(new RamlDocumentBuilder().build(raml), null, null);
        }

        public RamlMatcher using(JsonSchemaFactory jsonSchemaFactory) {
            return new RamlMatcher(raml, jsonSchemaFactory, null);
        }

        public RamlMatcher using(JsonSchemaValidatorSettings jsonSchemaValidatorSettings) {
            return new RamlMatcher(raml, null, jsonSchemaValidatorSettings);
        }

        @Override
        public void match(MvcResult result) throws Exception {
            Action action = assertRequest(result.getRequest());
            assertResponse(action, result.getResponse());
        }

        private Action assertRequest(MockHttpServletRequest request) {
            Resource resource = raml.getResource(request.getRequestURI());
            assertTrue("Resource " + request.getRequestURI() + " not defined in raml" + raml, resource != null);
            Action action = resource.getAction(request.getMethod());
            assertTrue("Action " + request.getMethod() + " not defined on resource " + resource, action != null);
            assertParameters(action, request);
            return action;
        }

        private void assertParameters(Action action, MockHttpServletRequest request) {
            Set<String> found = new HashSet<>();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                assertTrue("Query parameter " + entry.getKey() + " not defined on action " + action,
                        action.getQueryParameters().containsKey(entry.getKey()));
                assertTrue("Query parameter " + entry.getKey() + " on action " + action + " is not repeat but found repeatedly in response",
                        action.getQueryParameters().get(entry.getKey()).isRepeat() || entry.getValue().length == 1);
                found.add(entry.getKey());
            }
            for (Map.Entry<String, QueryParameter> entry : action.getQueryParameters().entrySet()) {
                assertTrue("Query parameter " + entry.getKey() + " on action " + action + " is required but not found in response", !entry.getValue().isRequired() || found.contains(entry.getKey()));
            }
        }

        private void assertResponse(Action action, MockHttpServletResponse response) {
            Response res = action.getResponses().get("" + response.getStatus());
            assertTrue("Response code " + response.getStatus() + " not defined on action " + action, res != null);
            MimeType mimeType = findMatchingMimeType(res, response.getContentType());
            assertTrue("Mime type " + response.getContentType() + " not defined on response " + res, mimeType != null);
            String schema = mimeType.getSchema();
            if (schema != null) {
                if (!schema.trim().startsWith("{")) {
                    schema = raml.getConsolidatedSchemas().get(mimeType.getSchema());
                    assertTrue("Schema " + mimeType.getSchema() + " referenced but not defined", schema != null);
                }
                try {
                    assertThat(response.getContentAsString(), validator(schema));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private Matcher<String> validator(String schema) {
            if (schemaFactory != null) {
                return (Matcher<String>) matchesJsonSchema(schema).using(schemaFactory);
            }
            if (schemaValidatorSettings != null) {
                return (Matcher<String>) matchesJsonSchema(schema).using(schemaValidatorSettings);
            }
            return matchesJsonSchema(schema);
        }

        private MimeType findMatchingMimeType(Response res, String toFind) {
            MediaType targetType = MediaType.parseMediaType(toFind);
            for (Map.Entry<String, MimeType> entry : res.getBody().entrySet()) {
                if (MediaType.parseMediaType(entry.getKey()).isCompatibleWith(targetType)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }
}
