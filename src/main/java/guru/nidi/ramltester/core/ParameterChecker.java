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
import guru.nidi.ramltester.util.Message;
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
    private static final String WILDCARD = "{?}";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlViolations violations;
    private final boolean acceptUndefined;
    private final boolean acceptWildcard;
    private final boolean ignoreX;
    private final boolean caseSensitive;
    private final Set<String> predefined;

    ParameterChecker(RamlViolations violations, boolean acceptUndefined, boolean acceptWildcard, boolean ignoreX, boolean caseSensitive, Set<String> predefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
        this.acceptWildcard = acceptWildcard;
        this.ignoreX = ignoreX;
        this.caseSensitive = caseSensitive;
        this.predefined = predefined;
    }

    ParameterChecker(RamlViolations violations) {
        this(violations, false, false, false, true, Collections.<String>emptySet());
    }

    ParameterChecker acceptUndefined() {
        return new ParameterChecker(violations, true, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker acceptWildcard() {
        return new ParameterChecker(violations, acceptUndefined, true, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker ignoreX(boolean ignoreX) {
        return new ParameterChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker caseSensitive(boolean caseSensitive) {
        return new ParameterChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker predefined(Set<String> predefined) {
        return new ParameterChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    public Set<String> checkParameters(Map<String, ? extends AbstractParam> params, Values values, Message message) {
        final Map<String, List<? extends AbstractParam>> listParams = new HashMap<>();
        addToMapOfList(params, listParams);
        return checkListParameters(listParams, values, message);
    }

    private void addToMapOfList(Map<String, ? extends AbstractParam> params, Map<String, List<? extends AbstractParam>> listParams) {
        for (final Map.Entry<String, ? extends AbstractParam> entry : params.entrySet()) {
            listParams.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
    }

    private boolean acceptUndefined(String name) {
        return acceptUndefined || predefined.contains(name) || (ignoreX && name.startsWith("x-"));
    }

    public Set<String> checkListParameters(Map<String, List<? extends AbstractParam>> params, Values values, Message message) {
        final Set<String> found = new HashSet<>();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final Message namedMsg = message.withParam(entry.getKey());
            final String paramName = findMatchingParamName(params.keySet(), entry.getKey());
            final List<? extends AbstractParam> parameters = params.get(paramName);
            if (parameters == null || parameters.isEmpty()) {
                violations.addIf(!acceptUndefined(entry.getKey().toLowerCase(Locale.ENGLISH)), namedMsg.withMessageParam("undefined"));
            } else {
                for (final AbstractParam parameter : parameters) {
                    violations.addIf(!parameter.isRepeat() && entry.getValue().size() > 1, namedMsg.withMessageParam("repeat.superfluous"));
                    for (final Object value : entry.getValue()) {
                        checkParameter(parameter, value, namedMsg);
                    }
                }
                found.add(paramName);
            }
        }
        for (final Map.Entry<String, List<? extends AbstractParam>> entry : params.entrySet()) {
            final Message namedMsg = message.withParam(entry.getKey());
            for (final AbstractParam parameter : entry.getValue()) {
                violations.addIf(parameter.isRequired() && !found.contains(entry.getKey()), namedMsg.withMessageParam("required.missing"));
            }
        }
        return found;
    }

    private String findMatchingParamName(Collection<String> paramNames, String name) {
        final String normalName = normalizeName(name);
        for (final String param : paramNames) {
            final String normalParam = normalizeName(param);
            final int pos = normalParam.indexOf(WILDCARD);
            if (normalParam.equals(normalName) || (acceptWildcard && pos >= 0 &&
                    nameMatchesKeyStart(normalName, normalParam, pos) && nameMatchesKeyEnd(normalName, normalParam, pos))) {
                return param;
            }
        }
        return null;
    }

    private String normalizeName(String name) {
        return caseSensitive ? name : name.toLowerCase(Locale.ENGLISH);
    }

    private boolean nameMatchesKeyStart(String name, String key, int wildcardPos) {
        return wildcardPos == 0 || name.startsWith(key.substring(0, wildcardPos));
    }

    private boolean nameMatchesKeyEnd(String name, String key, int wildcardPos) {
        return wildcardPos == key.length() - WILDCARD.length() ||
                name.endsWith(key.substring(wildcardPos + WILDCARD.length()));
    }

    public void checkParameter(AbstractParam param, Object value, Message message) {
        if (value == null) {
            final Message detail = message.withInnerParam(new Message("value", "empty"));
            checkNullParameter(param, detail);
        } else {
            final Message detail = message.withInnerParam(new Message("value", value));
            if (value instanceof String) {
                checkStringParameter(param, (String) value, detail);
            } else if (value instanceof FileValue) {
                checkFileParameter(param, detail);
            } else {
                throw new IllegalArgumentException("Unhandled parameter value '" + value + "' of type " + value.getClass());
            }
        }
    }

    private void checkNullParameter(AbstractParam param, Message detail) {
        if (param.getType() == ParamType.STRING) {
            checkStringParameter(param, "", detail);
        } else {
            violations.add(detail.withMessageParam("value.empty"));
        }
    }

    private void checkFileParameter(AbstractParam param, Message detail) {
        if (param.getType() != ParamType.FILE) {
            violations.add(detail.withMessageParam("file.superfluous", param.getType()));
        }
    }

    private void checkStringParameter(AbstractParam param, String value, Message detail) {
        switch (param.getType()) {
            case BOOLEAN:
                checkBoolean(value, detail);
                break;
            case DATE:
                checkDate(value, detail);
                break;
            case FILE:
                checkFile(detail);
                break;
            case INTEGER:
                checkInteger(param, value, detail);
                break;
            case NUMBER:
                checkNumber(param, value, detail);
                break;
            case STRING:
                checkString(param, value, detail);
                break;
            default:
                throw new RamlCheckerException("Unhandled parameter type '" + param.getType() + "'");
        }
    }

    private void checkString(AbstractParam param, String value, Message detail) {
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
    }

    private void checkNumber(AbstractParam param, String value, Message detail) {
        if (NUMBER.matcher(value).matches()) {
            if ("inf".equals(value) || "-inf".equals(value) || "nan".equals(value)) {
                violations.addIf(param.getMinimum() != null || param.getMaximum() != null, detail.withMessageParam("unbound"));
            } else {
                checkNumericLimits(param, new BigDecimal(value), detail);
            }
        } else {
            violations.add(detail.withMessageParam("number.invalid"));
        }
    }

    private void checkInteger(AbstractParam param, String value, Message detail) {
        if (INTEGER.matcher(value).matches()) {
            checkNumericLimits(param, new BigDecimal(value), detail);
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

    private void checkNumericLimits(AbstractParam param, BigDecimal value, Message message) {
        violations.addIf(param.getMinimum() != null && param.getMinimum().compareTo(value) > 0,
                message.withMessageParam("value.tooSmall", param.getMinimum()));
        violations.addIf(param.getMaximum() != null && param.getMaximum().compareTo(value) < 0,
                message.withMessageParam("value.tooBig", param.getMaximum()));
    }
}
