package guru.nidi.ramltester.junit;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;

/**
 *
 */
class EmptyMatcher extends TypeSafeMatcher<Collection<?>> {
    private final String desc;

    public EmptyMatcher(String desc) {
        this.desc = desc;
    }

    @Override
    protected boolean matchesSafely(Collection<?> item) {
        return item.isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(desc + " to be empty");
    }
}
