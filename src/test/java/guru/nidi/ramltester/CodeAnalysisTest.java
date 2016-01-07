/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester;

import edu.umd.cs.findbugs.Priorities;
import guru.nidi.codeassert.AnalyzerConfig;
import guru.nidi.codeassert.PackageCollector;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.pmd.CpdAnalyzer;
import guru.nidi.codeassert.pmd.MatchCollector;
import guru.nidi.codeassert.pmd.PmdAnalyzer;
import guru.nidi.codeassert.pmd.ViolationCollector;
import guru.nidi.ramltester.core.ParameterCheckerTest;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.util.MediaTypeTest;
import net.sourceforge.pmd.RulePriority;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.dependency.DependencyMatchers.hasNoCycles;
import static guru.nidi.codeassert.dependency.DependencyMatchers.matchesExactly;
import static guru.nidi.codeassert.findbugs.FindBugsMatchers.findsNoBugs;
import static guru.nidi.codeassert.pmd.PmdMatchers.hasNoDuplications;
import static guru.nidi.codeassert.pmd.PmdMatchers.hasNoPmdViolations;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class CodeAnalysisTest {
    private AnalyzerConfig withTest, withoutTest;

    @Before
    public void init() throws IOException {
        withTest = AnalyzerConfig.mavenMainAndTestClasses()
                .collecting(PackageCollector.all().including("guru.nidi.ramltester").excludingRest());
        withoutTest = AnalyzerConfig.mavenMainClasses()
                .collecting(PackageCollector.all().including("guru.nidi.ramltester").excludingRest());
    }

    @Test
    public void dependencies() {
        class GuruNidiRamltester implements DependencyRuler {
            DependencyRule $self, core, httpcomponents, restassured, junit, validator, model, servlet, spring, jaxrs, util;

            public void defineRules() {
                $self.mayDependUpon(model, core, servlet, httpcomponents, restassured, spring, jaxrs, validator, junit, util);
                core.mayDependUpon(model, util);
                util.mayDependUpon(model);
                servlet.mayDependUpon(model, util, core);
                httpcomponents.mayDependUpon(model, util, core);
                restassured.mayDependUpon(model, core);
                junit.mayDependUpon(util, core);
                validator.mayDependUpon(util, core);
                spring.mayDependUpon(model, util, core, servlet);
                jaxrs.mayDependUpon(model, util, core);
            }
        }

        final DependencyRules rules = DependencyRules.denyAll().withRules(new GuruNidiRamltester());
        assertThat(new ModelAnalyzer(withoutTest), matchesExactly(rules));
    }

    @Test
    public void noCircularDependencies() {
        assertThat(new ModelAnalyzer(withoutTest), hasNoCycles());
    }

    @Test
    public void findBugs() {
        final BugCollector collector = BugCollector.simple(null, Priorities.NORMAL_PRIORITY)
                .because("I don't agree").ignore(
                        "SBSC_USE_STRINGBUFFER_CONCATENATION").generally()
                .because("is in test").ignore(
                        "SIC_INNER_SHOULD_BE_STATIC_ANON", "SE_NO_SERIALVERSIONID", "DM_DEFAULT_ENCODING", "BC_UNCONFIRMED_CAST").in(
                        "*Test$*")
                .because("is in test").ignore(
                        "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "DM_DEFAULT_ENCODING", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR").in(
                        "*Test")
                .because("these are marked with TODOs").ignore(
                        "DM_DEFAULT_ENCODING").in(
                        "RestAssuredRamlRequest", "ServletRamlRequest", "ServletRamlResponse")
                .because("arrays are only used internally").ignore(
                        "EI_EXPOSE_REP", "EI_EXPOSE_REP2").in(
                        "*Response", "*Request")
                .because("it's class private and only used in 1 occasion").ignore(
                        "EQ_COMPARETO_USE_OBJECT_EQUALS").in(
                        "CheckerHelper$ResourceMatch");
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(withTest, collector);
        assertThat(analyzer, findsNoBugs());
    }

    @Test
    public void pmd() {
        final ViolationCollector collector = ViolationCollector.simple(RulePriority.MEDIUM)
                .because("makes no sense").ignore(
                        "JUnitSpelling").generally()
                .because("does not understand constants (logger is NOT)").ignore(
                        "VariableNamingConventions").generally()
                .because("I don't agree").ignore(
                        "UncommentedEmptyMethodBody", "AvoidFieldNameMatchingMethodName", "AvoidFieldNameMatchingTypeName",
                        "AbstractNaming", "UncommentedEmptyConstructor", "SimplifyStartsWith", "EmptyMethodInAbstractClassShouldBeAbstract",
                        "AvoidSynchronizedAtMethodLevel", "UseStringBufferForStringAppends").generally()
                .because("I don't agree").ignore(
                        "ConfusingTernary").in(
                        "SecurityExtractor$SchemeFinder")
                .because("I don't agree").ignore(
                        "NPathComplexity").in(
                        "UriComponents#getServer")
                .because("arrays are only used internally").ignore(
                        "MethodReturnsInternalArray", "ArrayIsStoredDirectly").in(
                        "*Response", "*Request")
                .because("not urgent and too many occasions").ignore(
                        "AvoidInstantiatingObjectsInLoops", "JUnitAssertionsShouldIncludeMessage", "JUnitTestContainsTooManyAsserts",
                        "MethodArgumentCouldBeFinal").generally()
                .because("it's plain wrong").ignore(
                        "ClassWithOnlyPrivateConstructorsShouldBeFinal").in(
                        "UsageCollector")
                .because("it's checked and correct").ignore(
                        "CompareObjectsWithEquals").in(
                        "RelativeJsonSchemaAwareRamlDocumentBuilder", "MediaType", "ServletRamlMessageTest")
                .because("it's checked and correct").ignore(
                        "PreserveStackTrace").in(
                        "JsRegex", "MediaType")
                .because("it's checked and correct").ignore(
                        "AvoidCatchingGenericException").in(
                        "JsRegex")
                .because("it's checked and correct").ignore(
                        "JUnitTestsShouldIncludeAssert").in(
                        UriTest.class, ParameterCheckerTest.class, MediaTypeTest.class)
                .because("it's style").ignore(
                        "CollapsibleIfStatements").in(
                        "RamlValidatorChecker")
                .because("It's marked with a TODO").ignore(
                        "GodClass").in(
                        "ParameterChecker", "Usage", "MediaType")
                .because("It's marked with a TODO").ignore(
                        "CyclomaticComplexity", "NPathComplexity").in(
                        "VariableMatcher", "MediaType")
                .because("It's marked with a TODO").ignore(
                        "AvoidDeeplyNestedIfStmts").in(
                        "ContentNegotiationChecker")
                .because("is in test").ignore(
                        "AvoidDuplicateLiterals", "SignatureDeclareThrowsException", "TooManyStaticImports", "AvoidDollarSigns").in(
                        "*Test", "*Test$*");
        final PmdAnalyzer analyzer = new PmdAnalyzer(withTest, collector)
                .withRuleSets(basic(), braces(), design(), exceptions(), imports(), junit(),
                        optimizations(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().tooManyMethods(35),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 25));
        assertThat(analyzer, hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        final MatchCollector collector = new MatchCollector()
                .because("there's no common superclass").ignore(
                        "DelegatingServletOutputStream", "DelegatingWriter")
                .because("It's marked with a TODO").ignore(
                        "SavingOutputStream", "DelegatingServletOutputStream", "DelegatingWriter")
                .because("java...").ignore(
                        "UsageCollector", "SecurityExtractor")
                .because("Similar but not same").ignore(
                        "*Request", "*Response")
                .because("Similar but not same").ignore(
                        RamlHttpClient.class)
                .because("Equals...").ignore(
                        "RamlViolations", "Values");
        final CpdAnalyzer analyzer = new CpdAnalyzer(withoutTest, 35, collector);
        assertThat(analyzer, hasNoDuplications());
    }
}
