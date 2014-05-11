package guru.nidi.ramltester;

/**
 *
 */
public class TestRaml {
    private TestRaml() {
    }

    public static RamlLoaders load(String name) {
        return new RamlLoaders(name, null, null);
    }
}
