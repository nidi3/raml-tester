package guru.nidi.ramltester;

import org.hamcrest.Matcher;

/**
 *
 */
public interface MatcherProvider<T> {
    Matcher<T> getMatcher(T data);
}
