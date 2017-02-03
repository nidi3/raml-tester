import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.CoverageCollector;
import guru.nidi.codeassert.jacoco.JacocoAnalyzer;
import org.junit.Test;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasEnoughCoverage;
import static org.junit.Assert.assertThat;

public class CodeCoverage {
    @Test
    public void coverage() {
        //TODO 75,75,75 should be the goal
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD)
                .just(For.global().setMinima(75, 75, 75))
                .just(For.allPackages().setMinima(75, 75, 75))
                .just(For.packge("*.ramltester").setMinima(75, 70, 55))
                .just(For.packge("*.model.internal").setMinima(65, 75, 75))
                .just(For.packge("*.httpcomponents").setMinima(55, 60, 65))
                .just(For.packge("*.jaxrs").setMinima(55, 60, 55))
                .just(For.packge("*.servlet").setMinima(70, 60, 75))
        );
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
