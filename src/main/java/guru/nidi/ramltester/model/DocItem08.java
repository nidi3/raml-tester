package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v08.api.DocumentationItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DocItem08 implements UnifiedDocItem {
    private DocumentationItem item;

    public DocItem08(DocumentationItem item) {
        this.item = item;
    }

    static List<UnifiedDocItem> of(List<DocumentationItem> items) {
        final List<UnifiedDocItem> res = new ArrayList<>();
        for (final DocumentationItem i : items) {
            res.add(new DocItem08(i));
        }
        return res;
    }

    @Override
    public String title() {
        return item.title();
    }

    @Override
    public String content() {
        return item.content().value();
    }
}
