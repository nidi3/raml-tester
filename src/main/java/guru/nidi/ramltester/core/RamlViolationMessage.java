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


public class RamlViolationMessage {
    private final String message;
    private final Object cause;

    public RamlViolationMessage(String message, Object cause) {
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public Object getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RamlViolationMessage message1 = (RamlViolationMessage) o;

        if (!message.equals(message1.message)) {
            return false;
        }
        return cause != null ? cause.equals(message1.cause) : message1.cause == null;

    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + (cause != null ? cause.hashCode() : 0);
        return result;
    }
}
