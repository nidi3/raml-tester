package guru.nidi.ramltester.apiportal;

/**
 *
 */
class ApiPortalFile {
    private String path;
    private String name;
    private String type;
    private String content;
    private String api_nid;
    private String ref_rfids;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getApi_nid() {
        return api_nid;
    }

    public void setApi_nid(String api_nid) {
        this.api_nid = api_nid;
    }

    public String getRef_rfids() {
        return ref_rfids;
    }

    public void setRef_rfids(String ref_rfids) {
        this.ref_rfids = ref_rfids;
    }
}
