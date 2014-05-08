package guru.nidi.ramltester;

/**
 *
 */
public class TestRaml {
    public static RamlLoaders load(String name) {
        return new RamlLoaders(name, null);
    }
}
