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
package guru.nidi.ramltester.core;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 *
 */
public class XmlSchemaViolationCause {
    private final List<Message> messages;

    public XmlSchemaViolationCause(SAXException e) {
        messages = singletonList(new Message(e.getMessage(), 0, 0));
    }

    public XmlSchemaViolationCause(List<SAXParseException> es) {
        messages = new ArrayList<>();
        for (final SAXParseException e : es) {
            messages.add(new Message(e.getMessage(), e.getLineNumber(), e.getColumnNumber()));
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public static class Message {
        private final String message;
        private final int line;
        private final int column;

        public Message(String message, int line, int column) {
            this.message = message;
            this.line = line;
            this.column = column;
        }

        public String getMessage() {
            return message;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }
    }
}
