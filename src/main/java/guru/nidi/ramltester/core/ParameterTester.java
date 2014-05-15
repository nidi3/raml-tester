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

import org.raml.model.parameter.AbstractParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
class ParameterTester {
    private static final Pattern INTEGER = Pattern.compile("0|-?[1-9][0-9]*");
    private static final Pattern NUMBER = Pattern.compile("0|inf|-inf|nan|-?(((0?|[1-9][0-9]*)\\.[0-9]*[1-9])|([1-9][0-9]*))(e[-+]?[1-9][0-9]*)?");
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlViolations violations;
    private final boolean acceptUndefined;

    ParameterTester(RamlViolations violations, boolean acceptUndefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
    }

    public void testParameters(Map<String, ? extends AbstractParam> params, Map<String, String[]> values, Message message) {
        Map<String, List<? extends AbstractParam>> listParams = new HashMap<>();
        for (Map.Entry<String, ? extends AbstractParam> entry : params.entrySet()) {
            listParams.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        testListParameters(listParams, values, message);
    }

    public void testListParameters(Map<String, List<? extends AbstractParam>> params, Map<String, String[]> values, Message message) {
        Set<String> found = new HashSet<>();
        for (Map.Entry<String, String[]> entry : values.entrySet()) {
            final Message namedMsg = message.withParam(entry.getKey());
            final List<? extends AbstractParam> parameters = params.get(entry.getKey());
            if (parameters == null || parameters.isEmpty()) {
                violations.addIf(!acceptUndefined, namedMsg.withMessageParam("undefined"));
            } else {
                for (AbstractParam parameter : parameters) {
                    violations.addIf(!parameter.isRepeat() && entry.getValue().length > 1, namedMsg.withMessageParam("repeat.superfluous"));
                    for (String value : entry.getValue()) {
                        testParameter(parameter, value, namedMsg);
                    }
                }
                found.add(entry.getKey());
            }
        }
        for (Map.Entry<String, List<? extends AbstractParam>> entry : params.entrySet()) {
            final Message namedMsg = message.withParam(entry.getKey());
            for (AbstractParam parameter : entry.getValue()) {
                violations.addIf(parameter.isRequired() && !found.contains(entry.getKey()), namedMsg.withMessageParam("required.missing"));
            }
        }
    }

    public void testParameter(AbstractParam param, String value, Message message) {
        Message detail = message.withInnerParam(new Message("value", value));
        switch (param.getType()) {
            case BOOLEAN:
                violations.addIf(!value.equals("true") && !value.equals("false"), detail.withMessageParam("boolean.invalid"));
                break;
            case DATE:
                try {
                    final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
                    dateFormat.setLenient(false);
                    dateFormat.parse(value);
                } catch (ParseException e) {
                    violations.add(detail.withMessageParam("date.invalid"));
                }
                break;
            case FILE:
                //TODO
                break;
            case INTEGER:
                if (INTEGER.matcher(value).matches()) {
                    testNumericLimits(param, new BigDecimal(value), detail);
                } else {
                    violations.add(detail.withMessageParam("integer.invalid"));
                }
                break;
            case NUMBER:
                if (NUMBER.matcher(value).matches()) {
                    if ((value.equals("inf") || value.equals("-inf") || value.equals("nan"))) {
                        violations.addIf(param.getMinimum() != null || param.getMaximum() != null, detail.withMessageParam("unbound"));
                    } else {
                        testNumericLimits(param, new BigDecimal(value), detail);
                    }
                } else {
                    violations.add(detail.withMessageParam("number.invalid"));
                }
                break;
            case STRING:
                violations.addIf(param.getEnumeration() != null && !param.getEnumeration().contains(value),
                        detail.withMessageParam("enum.invalid", param.getEnumeration()));
                try {
                    violations.addIf(param.getPattern() != null && !javaRegexOf(param.getPattern()).matcher(value).matches(),
                            detail.withMessageParam("pattern.invalid", param.getPattern()));
                } catch (PatternSyntaxException e) {
                    log.warn("Could not execute regex '" + param.getPattern(), e);
                }
                violations.addIf(param.getMinLength() != null && value.length() < param.getMinLength(),
                        detail.withMessageParam("length.tooSmall", param.getMinLength()));
                violations.addIf(param.getMaxLength() != null && value.length() > param.getMaxLength(),
                        detail.withMessageParam("length.tooBig", param.getMaxLength()));
                break;
        }
    }

    private void testNumericLimits(AbstractParam param, BigDecimal value, Message message) {
        violations.addIf(param.getMinimum() != null && param.getMinimum().compareTo(value) > 0,
                message.withMessageParam("value.tooSmall", param.getMinimum()));
        violations.addIf(param.getMaximum() != null && param.getMaximum().compareTo(value) < 0,
                message.withMessageParam("value.tooBig", param.getMaximum()));
    }

    private Pattern javaRegexOf(String regex) {
        if (isDoubleQuoted(regex) || isSingleQuoted(regex)) {
            regex = regex.substring(1, regex.length() - 1);
        }
        int flags = 0;
        if (regex.startsWith("/")) {
            int pos = regex.lastIndexOf("/");
            if (pos >= regex.length() - 3) {
                String flagString = pos == regex.length() - 1 ? "" : regex.substring(pos + 1);
                regex = regex.substring(1, pos);
                regex = regex.replace("\\/", "/");
                if (flagString.contains("i")) {
                    flags |= Pattern.CASE_INSENSITIVE;
                }
            }
        }
        return Pattern.compile(regex, flags);
    }

    private boolean isDoubleQuoted(String regex) {
        return regex.startsWith("\"") && regex.endsWith("\"");
    }

    private boolean isSingleQuoted(String regex) {
        return regex.startsWith("'") && regex.endsWith("'");
    }
}
