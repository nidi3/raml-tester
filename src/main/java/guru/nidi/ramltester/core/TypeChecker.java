/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.model.internal.RamlBody;
import guru.nidi.ramltester.model.internal.RamlType;
import guru.nidi.ramltester.util.Message;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.*;

import static guru.nidi.ramltester.core.CheckerHelper.typeNamesOf;
import static guru.nidi.ramltester.core.CheckerHelper.typesByName;

public class TypeChecker {
    private static final String WILDCARD = "{?}";

    private final RamlViolations violations;
    private final boolean acceptUndefined;
    private final boolean acceptWildcard;
    private final boolean ignoreX;
    private final boolean caseSensitive;
    private final boolean ignoreRequired;
    private final Set<String> predefined;

    TypeChecker(RamlViolations violations, boolean acceptUndefined, boolean acceptWildcard, boolean ignoreX, boolean caseSensitive, boolean ignoreRequired, Set<String> predefined) {
        this.violations = violations;
        this.acceptUndefined = acceptUndefined;
        this.acceptWildcard = acceptWildcard;
        this.ignoreX = ignoreX;
        this.caseSensitive = caseSensitive;
        this.ignoreRequired = ignoreRequired;
        this.predefined = predefined;
    }

    public TypeChecker(RamlViolations violations) {
        this(violations, false, false, false, true, false, Collections.<String>emptySet());
    }

    public TypeChecker acceptUndefined() {
        return new TypeChecker(violations, true, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeChecker acceptWildcard() {
        return new TypeChecker(violations, acceptUndefined, true, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeChecker ignoreX(boolean ignoreX) {
        return new TypeChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeChecker caseSensitive(boolean caseSensitive) {
        return new TypeChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public TypeChecker ignoreRequired() {
        return new TypeChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, true, predefined);
    }

    public TypeChecker predefined(Set<String> predefined) {
        return new TypeChecker(violations, acceptUndefined, acceptWildcard, ignoreX, caseSensitive, ignoreRequired, predefined);
    }

    public void check(RamlBody body, Values values, Message message) {
        //body08 is checked via formParameters
        final Object delegate = body.getDelegate();
        if (delegate instanceof TypeDeclaration) {
            //TODO use asYaml when resolved: https://github.com/raml-org/raml-java-parser/issues/294
            final RamlViolations jsonMsg = new RamlViolations();
            final String json = values.asJson();
            new Type10Checker(jsonMsg).check((TypeDeclaration) delegate, json, message);
            for (final RamlViolationMessage msg : jsonMsg) {
                violations.addRaw(msg.getMessage()
                        .replace("Error validating JSON. ", "")
                        .replace(json, values.toSimpleString()), msg.getCause());
            }
        }
    }

    public void check(RamlType type, Object value, Message message) {
        final Object delegate = type.getDelegate();
        if (delegate instanceof Parameter) {
            new Type08Checker(violations).check((Parameter) delegate, value, message);
        } else if (delegate instanceof TypeDeclaration) {
            new Type10Checker(violations).check((TypeDeclaration) delegate, value, message);
        }
    }

    //    public Set<String> check(List<RamlType> types, Values values, Message message) {
//
//    }
    public Set<String> check(List<RamlType> types, Values values, Message message) {
        final Set<String> found = new HashSet<>();
        final TypeChecker checker = new TypeChecker(violations);
        for (final Map.Entry<String, List<Object>> entry : values) {
            final Message namedMsg = message.withParam(entry.getKey());
            final String typeName = findMatchingTypeName(typeNamesOf(types), entry.getKey());
            final List<RamlType> ts = typesByName(types, typeName);
            if (ts == null || ts.isEmpty()) {
                violations.addIf(!acceptUndefined(entry.getKey().toLowerCase(Locale.ENGLISH)), namedMsg.withMessageParam("undefined"));
            } else {
                for (final RamlType t : ts) {
                    if (entry.getValue().size() == 1) {
                        checker.check(t, entry.getValue().get(0), namedMsg);
                    } else {
                        violations.addIf(!t.repeat(), namedMsg.withMessageParam("repeat.superfluous"));
                        checker.check(t, entry.getValue(), namedMsg);
                    }
                }
                found.add(typeName);
            }
        }
        for (final RamlType type : types) {
            final Message namedMsg = message.withParam(type.name());
            violations.addIf(type.required() && !ignoreRequired && !found.contains(type.name()), namedMsg.withMessageParam("required.missing"));
        }
        return found;
    }

    private String findMatchingTypeName(Collection<String> typeNames, String name) {
        final String normalName = normalizeName(name);
        for (final String typeName : typeNames) {
            final String normalType = normalizeName(typeName);
            final int pos = normalType.indexOf(WILDCARD);
            if (normalType.equals(normalName) || (acceptWildcard && pos >= 0 &&
                    nameMatchesKeyStart(normalName, normalType, pos) && nameMatchesKeyEnd(normalName, normalType, pos))) {
                return typeName;
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
