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


import guru.nidi.ramltester.model.RamlViolationCause;

public class RamlViolationMessage {
    private final String message;
    private final RamlViolationCause cause;

    public RamlViolationMessage(String message, RamlViolationCause cause) {
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public RamlViolationCause getCause() {
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
        if (!(o instanceof RamlViolationMessage)) {
            return false;
        }

        final RamlViolationMessage that = (RamlViolationMessage) o;

        return ( message == null && that.message == null )
                || (message != null && message.equals(that.getMessage()));
    }

    @Override
    public int hashCode() {
        if (message == null) {
            return 0;
        }
        return message.hashCode();
    }
}
