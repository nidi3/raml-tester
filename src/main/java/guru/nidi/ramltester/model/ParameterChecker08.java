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
package guru.nidi.ramltester.model;

import guru.nidi.ramltester.core.RamlCheckerException;
import guru.nidi.ramltester.core.RamlViolations;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import static guru.nidi.ramltester.core.CheckerHelper.paramNamesOf;
//import static guru.nidi.ramltester.core.CheckerHelper.paramsByName;

/**
 *
 */
class ParameterChecker08 {
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

    ParameterChecker08(RamlViolations violations, boolean acceptUndefined, boolean acceptWildcard, boolean ignoreX, boolean caseSensitive, Set<String> predefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
        this.acceptWildcard = acceptWildcard;
        this.ignoreX = ignoreX;
        this.caseSensitive = caseSensitive;
        this.predefined = predefined;
    }

    ParameterChecker08(RamlViolations violations) {
        this(violations, false, false, false, true, Collections.<String>emptySet());
    }

    ParameterChecker08 acceptUndefined() {
        return new ParameterChecker08(violations, true, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker08 acceptWildcard() {
        return new ParameterChecker08(violations, acceptUndefined, true, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker08 ignoreX(boolean ignoreX) {
        return new ParameterChecker08(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker08 caseSensitive(boolean caseSensitive) {
        return new ParameterChecker08(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    ParameterChecker08 predefined(Set<String> predefined) {
        return new ParameterChecker08(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, predefined);
    }

    public Set<String> checkParameters(List<Parameter> params, Values values, Message message) {
        final List<Parameter> listParams = new ArrayList<>();
//        addToMapOfList(params, listParams);
//        return checkListParameters(listParams, values, message);
        return checkListParameters(params, values, message);
    }

//    private void addToMapOfList(List<Parameter> params, Map<String, List<? extends AbstractParam>> listParams) {
//        for (final Map.Entry<String, ? extends AbstractParam> entry : params.entrySet()) {
//            listParams.put(entry.getKey(), Collections.singletonList(entry.getValue()));
//        }
//    }

    private boolean acceptUndefined(String name) {
        return acceptUndefined || predefined.contains(name) || (ignoreX && name.startsWith("x-"));
    }

    public Set<String> checkListParameters(List<Parameter> params, Values values, Message message) {
        final Set<String> found = new HashSet<>();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final Message namedMsg = message.withParam(entry.getKey());
            final String paramName = findMatchingParamName(paramNamesOf(params), entry.getKey());
            final List<Parameter> ps = paramsByName(params, paramName);
            if (ps == null || ps.isEmpty()) {
                violations.addIf(!acceptUndefined(entry.getKey().toLowerCase(Locale.ENGLISH)), namedMsg.withMessageParam("undefined"));
            } else {
                for (final Parameter parameter : ps) {
                    final boolean rep = parameter.repeat() != null && parameter.repeat();
                    violations.addIf(!rep && entry.getValue().size() > 1, namedMsg.withMessageParam("repeat.superfluous"));
                    for (final Object value : entry.getValue()) {
                        checkParameter(parameter, value, namedMsg);
                    }
                }
                found.add(paramName);
            }
        }
        for (final Parameter parameter : params) {
            final Message namedMsg = message.withParam(parameter.name());
//            for (final AbstractParam parameter : entry.getValue()) {
            violations.addIf(parameter.required() && !found.contains(parameter.name()), namedMsg.withMessageParam("required.missing"));
//            }
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

    public void checkParameter(Parameter param, Object value, Message message) {
        if (value instanceof Collection) {
            for (final Object v : (Collection<?>) value) {
                doCheckParameter(param, v, message);
            }
        } else {
            doCheckParameter(param, value, message);
        }
    }

    private void doCheckParameter(Parameter param, Object value, Message message) {
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

    private void checkNullParameter(Parameter param, Message detail) {
        if (param.type() == null || "string".equals(param.type())) {
            checkStringParameter(param, "", detail);
        } else {
            violations.add(detail.withMessageParam("value.empty"));
        }
    }

    private void checkFileParameter(Parameter param, Message detail) {
        if (!"file".equals(param.type())) {
            violations.add(detail.withMessageParam("file.superfluous", param.type()));
        }
    }

    private void checkStringParameter(Parameter param, String value, Message detail) {
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
                NumberTypeDeclaration numeric = (NumberTypeDeclaration) param;
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

    private List<String> paramNamesOf(List<Parameter> params) {
        final List<String> res = new ArrayList<>();
        for (final Parameter param : params) {
            res.add(param.name());
        }
        return res;
    }

    private List<Parameter> paramsByName(List<Parameter> parameters, String name) {
        final List<Parameter> res = new ArrayList<>();
        for (final Parameter parameter : parameters) {
            if (parameter.name().equals(name)) {
                res.add(parameter);
            }
        }
        return res;
    }

}
