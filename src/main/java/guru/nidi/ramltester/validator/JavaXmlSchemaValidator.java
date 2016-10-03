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
package guru.nidi.ramltester.validator;

import guru.nidi.loader.Loader;
import guru.nidi.loader.use.xml.LoaderLSResourceResolver;
import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class JavaXmlSchemaValidator implements SchemaValidator {
    private static final MediaType APPLICATION_XML = MediaType.valueOf("application/xml");
    private static final MediaType TEXT_XML = MediaType.valueOf("text/xml");

    private final Loader loader;

    private JavaXmlSchemaValidator(Loader loader) {
        this.loader = loader;
    }

    public JavaXmlSchemaValidator() {
        this(null);
    }

    @Override
    public SchemaValidator withLoader(Loader loader) {
        return new JavaXmlSchemaValidator(loader);
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return mediaType.isCompatibleWith(TEXT_XML) || mediaType.isCompatibleWith(APPLICATION_XML);
    }

    @Override
    public void validate(Reader content, Reader schema, RamlViolations violations, Message message) {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schemaFactory.setResourceResolver(new LoaderLSResourceResolver(loader));
            final Schema s = schemaFactory.newSchema(new StreamSource(schema));
            final Validator validator = s.newValidator();
            validator.setErrorHandler(new ViolationsWritingErrorHandler(violations, message));
            validator.validate(new StreamSource(content));
        } catch (SAXException | IOException e) {
            violations.add(message.withParam(e.getMessage()), e);
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
            violations.add(message.withMessageParam("javaXmlSchemaValidator.schema.warn",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()), e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            violations.add(message.withMessageParam("javaXmlSchemaValidator.schema.error",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()), e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            violations.add(message.withMessageParam("javaXmlSchemaValidator.schema.fatal",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()), e);
        }
    }

}
