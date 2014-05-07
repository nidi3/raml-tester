package guru.nidi.ramltester.spring;

/**
 *
 */
public class RamlResultMatchers {
    public static RequestResponseMatchers requestResponse() {
        return new RequestResponseMatchers(null);
    }
}
