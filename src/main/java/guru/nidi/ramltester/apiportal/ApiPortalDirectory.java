package guru.nidi.ramltester.apiportal;

import java.util.List;

/**
 *
 */
class ApiPortalDirectory {
    private String path;
    private String name;
    private String type;
    private List<ApiPortalFile> children;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ApiPortalFile> getChildren() {
        return children;
    }

    public void setChildren(List<ApiPortalFile> children) {
        this.children = children;
    }
}
