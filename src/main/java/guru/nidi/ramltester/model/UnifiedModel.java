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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class UnifiedModel {
    private UnifiedModel() {
    }

    public static UnifiedResponse responseByCode(List<UnifiedResponse> responses, String code) {
        for (final UnifiedResponse response : responses) {
            if (response.code().equals(code)) {
                return response;
            }
        }
        return null;
    }

    public static UnifiedType typeByName(List<UnifiedType> types, String name) {
        final List<UnifiedType> res = typesByName(types, name);
        if (res.size() > 1) {
            throw new IllegalArgumentException("Expected only one parameter with given name " + name);
        }
        return res.isEmpty() ? null : res.get(0);
    }

    public static List<UnifiedType> typesByName(List<UnifiedType> types, String name) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final UnifiedType type : types) {
            if (type.name().equals(name)) {
                res.add(type);
            }
        }
        return res;
    }

    public static List<String> typeNamesOf(List<UnifiedType> types) {
        final List<String> res = new ArrayList<>();
        for (final UnifiedType type : types) {
            res.add(type.name());
        }
        return res;
    }

    public static List<String> codesOf(List<UnifiedResponse> responses) {
        final List<String> res = new ArrayList<>();
        for (final UnifiedResponse response : responses) {
            res.add(response.code());
        }
        return res;
    }

}
