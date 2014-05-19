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
package guru.nidi.ramltester.loader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class RepositoryRamlLoader implements RamlLoader {
    private final RamlLoader loader;
    private final String responseName;
    private final RepositoryResponse response;
    private final Class<? extends RepositoryResponse> responseClass;

    public RepositoryRamlLoader(RamlLoader loader, String responseName, Class<? extends RepositoryResponse> responseClass) throws IOException {
        this.loader = loader;
        this.responseName = responseName;
        this.responseClass = responseClass;
        this.response = load();
    }

    @Override
    public InputStream fetchResource(String resourceName) {
        final RepositoryEntry entry = findEntry(resourceName);
        if (entry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        return new ByteArrayInputStream(entry.getContent().getBytes(Charset.forName("utf-8")));
    }

    protected RepositoryResponse load() throws IOException {
        final ObjectMapper mapper = createMapper();
        final InputStream files = loader.fetchResource(responseName);
        //TODO when empty, files is an empty array, not object!?
        return mapper.readValue(files, responseClass);
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private RepositoryEntry findEntry(String name) {
        for (RepositoryEntry file : response.getFiles()) {
            if (name.equals(file.getName()) || name.equals(file.getPath())) {
                return file;
            }
        }
        return null;
    }

}
