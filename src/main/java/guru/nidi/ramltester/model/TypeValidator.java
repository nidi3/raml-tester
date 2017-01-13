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

import guru.nidi.ramltester.core.RamlViolations;
import guru.nidi.ramltester.util.Message;

import java.util.*;

import static guru.nidi.ramltester.model.UnifiedModel.typeNamesOf;


/**
 *
 */
public class TypeValidator {
    private static final String WILDCARD = "{?}";

    private final RamlViolations violations;
    private final boolean acceptUndefined;
    private final boolean acceptWildcard;
    private final boolean ignoreX;
    private final boolean caseSensitive;
    private final boolean ignoreRequired;
    private final Set<String> predefined;

    TypeValidator(RamlViolations violations, boolean acceptUndefined, boolean acceptWildcard, boolean ignoreX, boolean caseSensitive, boolean ignoreRequired, Set<String> predefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
        this.acceptWildcard = acceptWildcard;
        this.ignoreX = ignoreX;
        this.caseSensitive = caseSensitive;
        this.ignoreRequired = ignoreRequired;
        this.predefined = predefined;
    }

    public TypeValidator(RamlViolations violations) {
        this(violations, false, false, false, true, false, Collections.<String>emptySet());
    }

    public TypeValidator acceptUndefined() {
        return new TypeValidator(violations, true, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeValidator acceptWildcard() {
        return new TypeValidator(violations, acceptUndefined, true, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeValidator ignoreX(boolean ignoreX) {
        return new TypeValidator(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeValidator caseSensitive(boolean caseSensitive) {
        return new TypeValidator(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeValidator ignoreRequired() {
        return new TypeValidator(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, true, predefined);
    }

    public TypeValidator predefined(Set<String> predefined) {
        return new TypeValidator(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public void validate(Object payload, Message message) {

    }

    public Set<String> validate(List<UnifiedType> params, Values values, Message message) {
        final Set<String> found = new HashSet<>();
        for (final Map.Entry<String, List<Object>> entry : values) {
            final Message namedMsg = message.withParam(entry.getKey());
            final String paramName = findMatchingParamName(typeNamesOf(params), entry.getKey());
            final List<UnifiedType> ps = typeNamesOf(params, paramName);
            if (ps == null || ps.isEmpty()) {
                violations.addIf(!acceptUndefined(entry.getKey().toLowerCase(Locale.ENGLISH)), namedMsg.withMessageParam("undefined"));
            } else {
                for (final UnifiedType parameter : ps) {
                    if (entry.getValue().size() == 1) {
                        parameter.validate(entry.getValue().get(0), violations, namedMsg);
                    } else {
                        violations.addIf(!parameter.repeat(), namedMsg.withMessageParam("repeat.superfluous"));
                        parameter.validate(entry.getValue(), violations, namedMsg);
                    }
                }
                found.add(paramName);
            }
        }
        for (final UnifiedType parameter : params) {
            final Message namedMsg = message.withParam(parameter.name());
//            for (final AbstractParam parameter : entry.getValue()) {
            violations.addIf(parameter.required() && !ignoreRequired && !found.contains(parameter.name()), namedMsg.withMessageParam("required.missing"));
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

    private boolean acceptUndefined(String name) {
        return acceptUndefined || predefined.contains(name) || (ignoreX && name.startsWith("x-"));
    }
}
