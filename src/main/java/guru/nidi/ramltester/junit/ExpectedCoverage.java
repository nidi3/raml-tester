package guru.nidi.ramltester.junit;

import guru.nidi.ramltester.CoverageProvider;
import guru.nidi.ramltester.core.CoverageItem;
import guru.nidi.ramltester.core.Message;
import guru.nidi.ramltester.core.RamlCoverage;
import org.junit.rules.Verifier;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertThat;

/**
 *
 */
public class ExpectedCoverage extends Verifier {
    private final CoverageProvider coverageProvider;
    private final EnumSet<CoverageItem> items;

    public ExpectedCoverage(CoverageProvider coverageProvider, CoverageItem... items) {
        this.coverageProvider = coverageProvider;
        this.items = items.length == 0 ? EnumSet.allOf(CoverageItem.class) : EnumSet.copyOf(Arrays.asList(items));
    }

    @Override
    protected void verify() throws Throwable {
        final RamlCoverage coverage = coverageProvider.getCoverage();
        for (CoverageItem item : items) {
            assertThat(item.get(coverage), new EmptyMatcher(new Message("coverage." + item.name()).toString()));
        }
    }
}
