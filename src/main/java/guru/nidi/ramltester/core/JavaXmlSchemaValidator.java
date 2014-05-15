package guru.nidi.ramltester.core;

import guru.nidi.ramltester.loader.RamlResourceLoader;
import guru.nidi.ramltester.loader.RamlResourceLoaderLSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 */
public class JavaXmlSchemaValidator implements SchemaValidator {
    private static final MediaType APPLICATION_XML = MediaType.valueOf("application/xml");
    private static final MediaType TEXT_XML = MediaType.valueOf("text/xml");

    private final RamlResourceLoader resourceLoader;

    private JavaXmlSchemaValidator(RamlResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public JavaXmlSchemaValidator() {
        this(null);
    }

    @Override
    public SchemaValidator withResourceLoader(RamlResourceLoader resourceLoader) {
        return new JavaXmlSchemaValidator(resourceLoader);
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.isCompatibleWith(TEXT_XML) || mediaType.isCompatibleWith(APPLICATION_XML);
    }

    @Override
    public void validate(String content, String schema, RamlViolations violations, Message message) {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schemaFactory.setResourceResolver(new RamlResourceLoaderLSResourceResolver(resourceLoader));
            final Schema s = schemaFactory.newSchema(new StreamSource(new StringReader(schema)));
            final Validator validator = s.newValidator();
            validator.setErrorHandler(new ViolationsWritingErrorHandler(violations, message));
            validator.validate(new StreamSource(new StringReader(content)));
        } catch (SAXException | IOException e) {
            violations.add(message.withParam(e.getMessage()));
        }
    }

    private static class ViolationsWritingErrorHandler implements ErrorHandler {
        private final RamlViolations violations;
        private final Message message;

        public ViolationsWritingErrorHandler(RamlViolations violations, Message message) {
            this.violations = violations;
            this.message = message;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            violations.add(message.withMessageParam("javaXmlSchemaValidator.schema.warn", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            violations.add(message.withMessageParam("javaXmlSchemaValidator.schema.error", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            violations.add(message.withMessageParam("javaXmlSchemaValidator.schema.fatal", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }
    }

}