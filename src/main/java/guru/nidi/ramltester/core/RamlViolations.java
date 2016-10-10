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
package guru.nidi.ramltester.core;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import guru.nidi.ramltester.violations.DefaultRamlValidationCause;
import guru.nidi.ramltester.violations.JsonSchemaRamlViolationCause;
import guru.nidi.ramltester.violations.XmlRamlViolationCause;
import guru.nidi.ramltester.util.Message;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class RamlViolations implements Iterable<RamlViolationMessage> {
    private final List<RamlViolationMessage> ramlViolationMessages;

    public RamlViolations() {
        ramlViolationMessages = new ArrayList<>();
    }

    public void add(Message message) {
        final RamlViolationMessage ramlViolationMessage = new RamlViolationMessage(message.toString(), new DefaultRamlValidationCause(message.toString()));
        add(message, ramlViolationMessage);
    }

    public void add(Message message, RamlViolationMessage ramlViolationMessage) {
        ramlViolationMessages.add(ramlViolationMessage);
    }

    public void add(Message message, ProcessingMessage processingMessage){
        final RamlViolationMessage ramlViolationMessage = new RamlViolationMessage(message.toString(), new JsonSchemaRamlViolationCause(processingMessage));
        add(message, ramlViolationMessage);
    }

    public void add(Message message, ProcessingException e) {
        final RamlViolationMessage ramlViolationMessage = new RamlViolationMessage(message.toString(), new JsonSchemaRamlViolationCause(e));
        add(message, ramlViolationMessage);
    }

    public void add(Message message, JsonParseException e) {
        try {
            final RamlViolationMessage ramlViolationMessage = new RamlViolationMessage(message.toString(), new JsonSchemaRamlViolationCause(e));
            add(message, ramlViolationMessage);
        } catch (JsonProcessingException ex){

            add(message, (IOException) ex);
        }
    }

    public void add(Message message, SAXException e) {
        final RamlViolationMessage ramlViolationMessage = new RamlViolationMessage(message.toString(), new XmlRamlViolationCause(e));
        add(message, ramlViolationMessage);
    }

    public void add(Message message, IOException e) {
        if(e instanceof JsonParseException){
            add(message, (JsonParseException)e);
        } else {
            final RamlViolationMessage ramlViolationMessage = new RamlViolationMessage(message.toString(), new DefaultRamlValidationCause(e.getMessage()));
            add(message, ramlViolationMessage);
        }
    }

    void add(String key, Object... params) {
        add(new Message(key, params));
    }

    public void addIf(boolean condition, Message message) {
        if (condition) {
            add(message);
        }
    }

    void addIf(boolean condition, String key, Object... params) {
        addIf(condition, new Message(key, params));
    }

    void addAll(RamlViolations violations) {
        this.ramlViolationMessages.addAll(violations.ramlViolationMessages);
    }

    public int size() {
        return ramlViolationMessages.size();
    }

    public boolean isEmpty() {
        return ramlViolationMessages.isEmpty();
    }

    public List<String> asList() {
        return ramlViolationMessages.stream()
                .map(ramlViolationMessage -> ramlViolationMessage.getMessage())
                .collect(Collectors.toList());
    }

    public List<RamlViolationMessage> asMessages() {
        return ramlViolationMessages;
    }

    @Override
    public Iterator<RamlViolationMessage> iterator() {
        return ramlViolationMessages.iterator();
    }

    @Override
    public String toString() {
        return ramlViolationMessages.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RamlViolations that = (RamlViolations) o;
        return ramlViolationMessages.equals(that.ramlViolationMessages);

    }

    @Override
    public int hashCode() {
        return ramlViolationMessages.hashCode();
    }

}
