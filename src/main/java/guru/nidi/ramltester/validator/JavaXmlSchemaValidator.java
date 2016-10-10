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
import guru.nidi.ramltester.core.XmlSchemaViolationCause;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

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
        try {
            final SchemaFactory schemaFactory = LoaderLSResourceResolver.createXmlSchemaFactory(loader);
            final Schema s = schemaFactory.newSchema(new StreamSource(schema));
            final Validator validator = s.newValidator();
            final ViolationsWritingErrorHandler errorHandler = new ViolationsWritingErrorHandler();
            validator.setErrorHandler(errorHandler);
            validator.validate(new StreamSource(content));
            if (!errorHandler.getExceptions().isEmpty()) {
                String msg = "";
                for (final SAXParseException ex : errorHandler.getExceptions()) {
                    msg += new Message("javaXmlSchemaValidator.message", ex.getLineNumber(), ex.getColumnNumber(), ex.getMessage());
                }
                violations.add(message.withParam(msg), new XmlSchemaViolationCause(errorHandler.getExceptions()));
            }
        } catch (SAXException e) {
            violations.add(message.withParam(new Message("schema.invalid", e.getMessage())), new XmlSchemaViolationCause(e));
        } catch (IOException e) {
            violations.add(message.withParam(new Message("schema.invalid", e.getMessage())));
        }
    }

    private static class ViolationsWritingErrorHandler implements ErrorHandler {
        private final List<SAXParseException> exceptions = new ArrayList<>();

        @Override
        public void warning(SAXParseException e) throws SAXException {
            exceptions.add(e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            exceptions.add(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            exceptions.add(e);
        }

        public List<SAXParseException> getExceptions() {
            return exceptions;
        }
    }

}
