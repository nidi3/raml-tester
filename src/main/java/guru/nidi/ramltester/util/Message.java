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
package guru.nidi.ramltester.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

public class Message {
    private static final Properties MESSAGES;

    static {
        MESSAGES = new Properties();
        try {
            MESSAGES.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("guru/nidi/ramltester/messages.properties"));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    protected final String key;
    protected final Object[] params;

    public Message(String key, Object... params) {
        this.key = key;
        this.params = params;
    }

    public Message withMessageParam(String key, Object... params) {
        return withParam(new Message(key, params));
    }

    public Message withInnerParam(final Message param) {
        return new InnerMessage(key, addParam(param));
    }

    public Message withParam(Object param) {
        return new Message(key, addParam(param));
    }

    private Object[] addParam(Object param) {
        final Object[] newParams = new Object[params.length + 1];
        System.arraycopy(params, 0, newParams, 0, params.length);
        newParams[newParams.length - 1] = param;
        return newParams;
    }

    @Override
    public String toString() {
        final String pattern = MESSAGES.getProperty(key);
        return MessageFormat.format(pattern == null ? key : pattern, params);
    }

    private static class InnerMessage extends Message {
        public InnerMessage(String key, Object... params) {
            super(key, params);
        }

        @Override
        public Message withParam(Object p) {
            final Object[] newParams = new Object[params.length];
            System.arraycopy(params, 0, newParams, 0, params.length);
            newParams[params.length - 1] = ((Message) newParams[params.length - 1]).withParam(p);
            return new Message(key, newParams);
        }
    }
}
