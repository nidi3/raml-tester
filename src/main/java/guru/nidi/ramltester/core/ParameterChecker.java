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

import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.util.FileValue;
import org.raml.model.ParamType;
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
class ParameterChecker {
    private static final Pattern INTEGER = Pattern.compile("0|-?[1-9][0-9]*");
    private static final Pattern NUMBER = Pattern.compile("0|inf|-inf|nan|-?(((0?|[1-9][0-9]*)\\.[0-9]*[1-9])|([1-9][0-9]*))(e[-+]?[1-9][0-9]*)?");
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlViolations violations;
    private final boolean acceptUndefined;
    private final boolean acceptWildcard;
    private final Set<String> predefined;

    ParameterChecker(RamlViolations violations, boolean acceptUndefined, boolean acceptWildcard, Set<String> predefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
        this.acceptWildcard = acceptWildcard;
        this.predefined = predefined;
    }

    ParameterChecker(RamlViolations violations) {
        this(violations, false, false, Collections.<String>emptySet());
    }

    ParameterChecker acceptUndefined() {
        return new ParameterChecker(violations, true, acceptWildcard, predefined);
    }

    ParameterChecker acceptWildcard() {
        return new ParameterChecker(violations, acceptUndefined, true, predefined);
    }

    ParameterChecker predefined(Set<String> predefined) {
        return new ParameterChecker(violations, acceptUndefined, acceptWildcard, predefined);
    }

    public Set<String> checkParameters(Map<String, ? extends AbstractParam> params, Values values, Message message) {
        Map<String, List<? extends AbstractParam>> listParams = new HashMap<>();
        for (Map.Entry<String, ? extends AbstractParam> entry : params.entrySet()) {
            listParams.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        return checkListParameters(listParams, values, message);
    }

    public Set<String> checkListParameters(Map<String, List<? extends AbstractParam>> params, Values values, Message message) {
        Set<String> found = new HashSet<>();
        for (Map.Entry<String, List<Object>> entry : values) {
            final Message namedMsg = message.withParam(entry.getKey());
            final String paramName = findMatchingParamName(params.keySet(), entry.getKey());
            final List<? extends AbstractParam> parameters = params.get(paramName);
            if (parameters == null || parameters.isEmpty()) {
                violations.addIf(!acceptUndefined && !predefined.contains(entry.getKey().toLowerCase()), namedMsg.withMessageParam("undefined"));
            } else {
                for (AbstractParam parameter : parameters) {
                    violations.addIf(!parameter.isRepeat() && entry.getValue().size() > 1, namedMsg.withMessageParam("repeat.superfluous"));
                    for (Object value : entry.getValue()) {
                        checkParameter(parameter, value, namedMsg);
                    }
                }
                found.add(paramName);
            }
        }
        for (Map.Entry<String, List<? extends AbstractParam>> entry : params.entrySet()) {
            final Message namedMsg = message.withParam(entry.getKey());
            for (AbstractParam parameter : entry.getValue()) {
                violations.addIf(parameter.isRequired() && !found.contains(entry.getKey()), namedMsg.withMessageParam("required.missing"));
            }
        }
        return found;
    }

    private String findMatchingParamName(Collection<String> paramNames, String name) {
        if (!acceptWildcard) {
            return name;
        }
        for (String key : paramNames) {
            int pos = key.indexOf("{?}");
            if (pos >= 0) {
                if ((pos == 0 || name.startsWith(key.substring(0, pos))) &&
                        (pos == key.length() - 3 || name.endsWith(key.substring(pos + 3)))) {
                    return key;
                }
            } else if (key.equals(name)) {
                return key;
            }
        }
        return null;
    }

    public void checkParameter(AbstractParam param, Object value, Message message) {
        if (value == null) {
            Message detail = message.withInnerParam(new Message("value", "empty"));
            checkNullParameter(param, detail);
        } else {
            Message detail = message.withInnerParam(new Message("value", value));
            if (value instanceof String) {
                checkStringParameter(param, (String) value, detail);
            } else if (value instanceof FileValue) {
                checkFileParameter(param, (FileValue) value, detail);
            } else {
                throw new IllegalArgumentException("Unhandled parameter value '" + value + "' of type " + value.getClass());
            }
        }
    }

    private void checkNullParameter(AbstractParam param, Message detail) {
        if (param.getType() != ParamType.STRING) {
            violations.add(detail.withMessageParam("value.empty"));
        } else {
            checkStringParameter(param, "", detail);
        }
    }

    private void checkFileParameter(AbstractParam param, FileValue value, Message detail) {
        if (param.getType() != ParamType.FILE) {
            violations.add(detail.withMessageParam("file.superfluous", param.getType()));
        }
    }

    private void checkStringParameter(AbstractParam param, String value, Message detail) {
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
                violations.add(detail.withMessageParam("file.invalid"));
                break;
            case INTEGER:
                if (INTEGER.matcher(value).matches()) {
                    checkNumericLimits(param, new BigDecimal(value), detail);
                } else {
                    violations.add(detail.withMessageParam("integer.invalid"));
                }
                break;
            case NUMBER:
                if (NUMBER.matcher(value).matches()) {
                    if ((value.equals("inf") || value.equals("-inf") || value.equals("nan"))) {
                        violations.addIf(param.getMinimum() != null || param.getMaximum() != null, detail.withMessageParam("unbound"));
                    } else {
                        checkNumericLimits(param, new BigDecimal(value), detail);
                    }
                } else {
                    violations.add(detail.withMessageParam("number.invalid"));
                }
                break;
            case STRING:
                violations.addIf(param.getEnumeration() != null && !param.getEnumeration().contains(value),
                        detail.withMessageParam("enum.invalid", param.getEnumeration()));
                try {
                    violations.addIf(param.getPattern() != null && !JsRegex.matches(value, param.getPattern()),
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

    private void checkNumericLimits(AbstractParam param, BigDecimal value, Message message) {
        violations.addIf(param.getMinimum() != null && param.getMinimum().compareTo(value) > 0,
                message.withMessageParam("value.tooSmall", param.getMinimum()));
        violations.addIf(param.getMaximum() != null && param.getMaximum().compareTo(value) < 0,
                message.withMessageParam("value.tooBig", param.getMaximum()));
    }
}
