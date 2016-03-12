package guru.nidi.ramltester.validator;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.library.DraftV4Library;
import com.github.fge.jsonschema.library.Keyword;
import com.github.fge.jsonschema.library.Library;
import com.github.fge.jsonschema.library.LibraryBuilder;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonSchemaFactoryBuilder;
import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.util.Message;
import org.junit.Test;

import java.io.StringReader;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 *
 */
public class JsonSchemaValidatorTest {
    @Test
    public void simpleOk() {
        final RamlViolations violations = validate("42", "{'type':'number'}");
        assertTrue(violations.isEmpty());
    }

    @Test
    public void simpleNok() {
        final RamlViolations violations = validate("42", "{'type':'string'}");
        assertEquals(1, violations.size());
        assertThat(violations.iterator().next(), startsWith("Body does not match schema for test\n" +
                "Content: 42\n" +
                "Message: error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])\n"));
    }

    @Test
    public void property() {
        final RamlViolations violations = validate("{'name':'bla'}", "{'type':'object','properties':{'name':{'type':'string'}}}");
        assertTrue(violations.isEmpty());
    }

    @Test
    public void optionalPropertyAbsent() {
        final RamlViolations violations = validate("{}", "{'type':'object','properties':{'name':{'type':'string'}}}");
        assertTrue(violations.isEmpty());
    }

    @Test
    public void optionalPropertyNull() {
        final RamlViolations violations = validate("{'name':null,'other':'bla'}", "{'$schema':'http://test','type':'object','properties':{'name':{'type':'string'},'other':{}},'required':['other']}");
        assertTrue(violations.isEmpty());
    }

    private RamlViolations validate(String input, String schema) {
        final RamlViolations violations = new RamlViolations();
        final Library lib = DraftV4Library.get();
        final LibraryBuilder lb = lib.thaw();
        lb.addKeyword(Keyword.newBuilder("type")
                .withSyntaxChecker(lib.getSyntaxCheckers().entries().get("type"))
                .withDigester(lib.getDigesters().entries().get("type"))
                .withValidatorClass(MyTypeValidator.class).freeze());
        final JsonSchemaFactoryBuilder jsf = JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(ValidationConfiguration.newBuilder()
                        .addLibrary("http://test",lb.freeze()) //SchemaVersion.DRAFTV4.getLocation().toString()
                        .freeze());
        new JsonSchemaValidator()
                .using(jsf.freeze())
                .validate(
                        new StringReader(input.replace('\'', '"')),
                        new StringReader(schema.replace('\'', '"')),
                        violations, new Message("schema.body.mismatch", "test", input));
        return violations;
    }
}
