package guru.nidi.ramltester.apiportal;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlRepository;
import guru.nidi.ramltester.SchemaValidator;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.util.Iterator;

/**
 *
 */
class ApiPortalRamlRepository implements RamlRepository {
    private final ApiPortalFilesResponse filesResponse;
    private final SchemaValidator schemaValidator;

    ApiPortalRamlRepository(ApiPortalFilesResponse filesResponse, SchemaValidator schemaValidator) {
        this.filesResponse = filesResponse;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public RamlRepository withSchemaValidator(SchemaValidator schemaValidator) {
        return new ApiPortalRamlRepository(filesResponse, schemaValidator);
    }

    @Override
    public RamlDefinition getRaml(String name) {
        for (ApiPortalFile file : filesResponse.getFiles().values()) {
            if (file.getName().equals(name)) {
                return new RamlDefinition(new RamlDocumentBuilder(new ClassPathResourceLoader()).build(file.getContent(), file.getPath()), schemaValidator);
                //TODO ApiPortalRamlRepository ResourceLoader
            }
        }
        return null;
    }

    @Override
    public Iterable<String> getNames() {
        final Iterator<ApiPortalFile> fileIterator = filesResponse.getFiles().values().iterator();
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return fileIterator.hasNext();
                    }

                    @Override
                    public String next() {
                        return fileIterator.next().getName();
                    }
                };
            }
        };
    }
}
