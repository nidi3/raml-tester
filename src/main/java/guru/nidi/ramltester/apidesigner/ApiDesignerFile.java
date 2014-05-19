package guru.nidi.ramltester.apidesigner;

/**
 *
 */
public class ApiDesignerFile implements ApiFile {
    private String name;
    private String path;
    private String contents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getContent() {
        return getContents();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "ApiDesignerFile{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
