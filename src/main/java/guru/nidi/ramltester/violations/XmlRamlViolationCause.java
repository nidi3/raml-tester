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
package guru.nidi.ramltester.violations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.ramltester.model.RamlViolationCause;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import static java.lang.String.format;

/**
 * Created by arielsegura on 10/9/16.
 */
public class XmlRamlViolationCause implements RamlViolationCause{

    XmlError error;
    ObjectMapper mapper = new ObjectMapper();

    public XmlRamlViolationCause(SAXParseException e) {
        error = new XmlError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
    }

    public XmlRamlViolationCause(SAXException e) {
        error = new XmlError(e.getMessage(), 0, 0);
    }

    @Override
    public String getMessage() {
        return error.toString();
    }

    @Override
    public String asJson() throws JsonProcessingException {
        return mapper.writeValueAsString(error);
    }

    private static class XmlError {
        String message;
        int line;
        int column;

        public XmlError() {
        }

        public XmlError(String message, int line, int column) {
            this.message = message;
            this.line = line;
            this.column = column;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        @Override
        public String toString(){
            return format("%s. Line: %s, Column: %s. ", message, line, column);
        }
    }
}
