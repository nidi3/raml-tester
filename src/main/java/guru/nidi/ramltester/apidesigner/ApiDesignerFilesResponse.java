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
package guru.nidi.ramltester.apidesigner;

import guru.nidi.ramltester.loader.RepositoryEntry;
import guru.nidi.ramltester.loader.RepositoryResponse;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ApiDesignerFilesResponse extends AbstractMap<String,ApiDesignerFile> implements RepositoryResponse {
    private final Map<String,ApiDesignerFile> files=new HashMap<>();
    
    @Override
    public Iterable<? extends RepositoryEntry> getFiles() {
        return files.values();
    }

    @Override
    public ApiDesignerFile put(String key, ApiDesignerFile value) {
        return files.put(key, value);
    }

    @Override
    public Set<Entry<String, ApiDesignerFile>> entrySet() {
        return files.entrySet();
    }
}
