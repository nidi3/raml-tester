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

import guru.nidi.ramltester.util.FileValue;
import guru.nidi.ramltester.util.Message;
import org.raml.v2.api.model.v08.parameters.IntegerTypeDeclaration;
import org.raml.v2.api.model.v08.parameters.NumberTypeDeclaration;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.parameters.StringTypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
class Type08Checker {
    private static final Pattern INTEGER = Pattern.compile("0|-?[1-9][0-9]*");
    private static final Pattern NUMBER = Pattern.compile("0|inf|-inf|nan|-?(((0?|[1-9][0-9]*)\\.[0-9]*[1-9])|([1-9][0-9]*))(e[-+]?[1-9][0-9]*)?");
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlViolations violations;

    public Type08Checker(RamlViolations violations) {
        this.violations = violations;
    }

    public void check(Parameter param, Object value, Message message) {
        if (value instanceof Collection) {
            for (final Object v : (Collection<?>) value) {
                doCheck(param, v, message);
            }
        } else {
            doCheck(param, value, message);
        }
    }

    private void doCheck(Parameter param, Object value, Message message) {
        if (value == null) {
            final Message detail = message.withInnerParam(new Message("value", "empty"));
            checkNull(param, detail);
        } else {
            final Message detail = message.withInnerParam(new Message("value", value));
            if (value instanceof String) {
                checkString(param, (String) value, detail);
            } else if (value instanceof FileValue) {
                checkFile(param, detail);
            } else {
                throw new IllegalArgumentException("Unhandled parameter value '" + value + "' of type " + value.getClass());
            }
        }
    }

    private void checkNull(Parameter param, Message detail) {
        if (param.type() == null || "string".equals(param.type())) {
            checkString(param, "", detail);
        } else {
            violations.add(detail.withMessageParam("value.empty"));
        }
    }

    private void checkFile(Parameter param, Message detail) {
        if (!"file".equals(param.type())) {
            violations.add(detail.withMessageParam("file.superfluous", param.type()));
        }
    }

    private void checkString(Parameter param, String value, Message detail) {
        if (param.type() == null) {
            return;
        }
        if ("boolean".equals(param.type())) {
            checkBoolean(value, detail);
        } else if ("date".equals(param.type())) {
            checkDate(value, detail);
        } else if ("file".equals(param.type())) {
            checkFile(detail);
        } else if ("integer".equals(param.type())) {
            checkInteger(param, value, detail);
        } else if ("number".equals(param.type())) {
            checkNumber(param, value, detail);
        } else if (param instanceof StringTypeDeclaration) {
            checkString((StringTypeDeclaration) param, value, detail);
        } else {
            throw new RamlCheckerException("Unhandled parameter type '" + param.type() + "'");
        }
    }

    private void checkString(StringTypeDeclaration param, String value, Message detail) {
        violations.addIf(param.enumValues() != null && !param.enumValues().isEmpty() && !param.enumValues().contains(value),
                detail.withMessageParam("enum.invalid", param.enumValues()));
        try {
            violations.addIf(param.pattern() != null && !JsRegex.matches(value, param.pattern()),
                    detail.withMessageParam("pattern.invalid", param.pattern()));
        } catch (PatternSyntaxException e) {
            log.warn("Could not execute regex '" + param.pattern(), e);
        }
        violations.addIf(param.minLength() != null && value.length() < param.minLength(),
                detail.withMessageParam("length.tooSmall", param.minLength()));
        violations.addIf(param.maxLength() != null && value.length() > param.maxLength(),
                detail.withMessageParam("length.tooBig", param.maxLength()));
    }

    private void checkNumber(Parameter param, String value, Message detail) {
        if (NUMBER.matcher(value).matches()) {
            if (param instanceof NumberTypeDeclaration) {
                final NumberTypeDeclaration numeric = (NumberTypeDeclaration) param;
                checkNumericLimits(numeric, Double.parseDouble(value), detail);
            }
        } else {
            violations.add(detail.withMessageParam("number.invalid"));
        }
    }

    private void checkInteger(Parameter param, String value, Message detail) {
        if (INTEGER.matcher(value).matches()) {
            if (param instanceof IntegerTypeDeclaration) {
                checkNumericLimits((NumberTypeDeclaration) param, Double.parseDouble(value), detail);
            }
        } else {
            violations.add(detail.withMessageParam("integer.invalid"));
        }
    }

    private void checkFile(Message detail) {
        violations.add(detail.withMessageParam("file.invalid"));
    }

    private void checkDate(String value, Message detail) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            dateFormat.setLenient(false);
            dateFormat.parse(value);
        } catch (ParseException e) {
            violations.add(detail.withMessageParam("date.invalid"));
        }
    }

    private void checkBoolean(String value, Message detail) {
        violations.addIf(!"true".equals(value) && !"false".equals(value), detail.withMessageParam("boolean.invalid"));
    }

    private void checkNumericLimits(NumberTypeDeclaration param, double value, Message message) {
        violations.addIf(param.minimum() != null && param.minimum().compareTo(value) > 0,
                message.withMessageParam("value.tooSmall", param.minimum()));
        violations.addIf(param.maximum() != null && param.maximum().compareTo(value) < 0,
                message.withMessageParam("value.tooBig", param.maximum()));
    }
}
