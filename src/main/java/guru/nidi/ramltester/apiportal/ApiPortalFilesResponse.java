package guru.nidi.ramltester.apiportal;

import java.util.Map;

/**
 *
 */
class ApiPortalFilesResponse {
    private Map<String, ApiPortalFile> files;
    private ApiPortalDirectory directory;

    public Map<String, ApiPortalFile> getFiles() {
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
}
