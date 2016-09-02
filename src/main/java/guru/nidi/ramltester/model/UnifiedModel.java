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
public class UnifiedModel {
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

    public static UnifiedType typeByName(List<UnifiedType> parameters, String name) {
        final List<UnifiedType> res = typesByName(parameters, name);
        if (res.size() > 1) {
            throw new IllegalArgumentException("Expected only one parameter with given name " + name);
        }
        return res.isEmpty() ? null : res.get(0);
    }

    public static List<UnifiedType> typesByName(List<UnifiedType> parameters, String name) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final UnifiedType parameter : parameters) {
            if (parameter.name().equals(name)) {
                res.add(parameter);
            }
        }
        return res;
    }

    public static List<String> typeNamesOf(List<UnifiedType> params) {
        final List<String> res = new ArrayList<>();
        for (final UnifiedType param : params) {
            res.add(param.name());
        }
        return res;
    }

    public static List<UnifiedType> typeNamesOf(List<UnifiedType> parameters, String name) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final UnifiedType parameter : parameters) {
            if (parameter.name().equals(name)) {
                res.add(parameter);
            }
        }
        return res;
    }

    public static List<String> codesOf(List<UnifiedResponse> params) {
        final List<String> res = new ArrayList<>();
        for (final UnifiedResponse param : params) {
            res.add(param.code());
        }
        return res;
    }

}
