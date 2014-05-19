package guru.nidi.ramltester.apidesigner;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ApiDesignerFilesResponse extends AbstractMap<String,ApiDesignerFile> implements ApiFilesResponse {
    private final Map<String,ApiDesignerFile> files=new HashMap<>();
    
    @Override
    public Iterable<? extends ApiFile> getFiles() {
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
