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

import java.util.Map;

/**
 *
 */
class ApiPortalFilesResponse implements ApiFilesResponse {
    private Map<String, ApiPortalFile> files;
    private ApiPortalDirectory directory;

    public Map<String, ApiPortalFile> getFilesMap() {
        return files;
    }

    public void setFiles(Map<String, ApiPortalFile> files) {
        this.files = files;
    }

    public ApiPortalDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(ApiPortalDirectory directory) {
        this.directory = directory;
    }

    @Override
    public Iterable<? extends ApiFile> getFiles() {
        return files.values();
    }
}
