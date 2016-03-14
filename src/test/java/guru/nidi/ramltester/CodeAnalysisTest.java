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
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.junit.CodeAssertTest;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.model.ModelResult;
import guru.nidi.codeassert.pmd.*;
import guru.nidi.ramltester.core.ParameterCheckerTest;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.util.MediaTypeTest;
import net.sourceforge.pmd.RulePriority;
import org.junit.Test;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.packagesMatchExactly;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class CodeAnalysisTest extends CodeAssertTest {
    @Test
    public void dependencies() {
        class GuruNidiRamltester extends DependencyRuler {
            DependencyRule $self, core, httpcomponents, restassured, junit, validator, model, servlet, spring, jaxrs, util;

            public void defineRules() {
                $self.mayUse(model, core, servlet, httpcomponents, restassured, spring, jaxrs, validator, junit, util);
                core.mayUse(model, util);
                util.mayUse(model);
                servlet.mayUse(model, util, core);
                httpcomponents.mayUse(model, util, core);
                restassured.mayUse(model, core);
                junit.mayUse(util, core);
                validator.mayUse(util, core);
                spring.mayUse(model, util, core, servlet);
                jaxrs.mayUse(model, util, core);
            }
        }
//TODO dependencies to externals (spring, httpcomponents etc.)
        final DependencyRules rules = DependencyRules.denyAll()
                .withExternals("java*", "org*", "com*", "guru.nidi.loader*")
                .withRelativeRules(new GuruNidiRamltester());
        assertThat(modelResult(), packagesMatchExactly(rules));
    }

    @Override
    protected ModelResult analyzeModel() {
        return new ModelAnalyzer(AnalyzerConfig.maven().main()).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        final BugCollector collector = new BugCollector().minPriority(Priorities.NORMAL_PRIORITY)
                .because("I don't agree",
                        In.everywhere().ignore("SBSC_USE_STRINGBUFFER_CONCATENATION"))
                .because("it's in test",
                        In.loc("*Test$*")
                                .ignore("SIC_INNER_SHOULD_BE_STATIC_ANON", "SE_NO_SERIALVERSIONID", "DM_DEFAULT_ENCODING", "BC_UNCONFIRMED_CAST"),
                        In.loc("*Test")
                                .ignore("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "DM_DEFAULT_ENCODING", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"))
                .because("TODO",                 //TODO
                        In.locs("RestAssuredRamlRequest", "ServletRamlRequest", "ServletRamlResponse").ignore("DM_DEFAULT_ENCODING"))
                .because("arrays are only used internally",
                        In.locs("*Response", "*Request").ignore("EI_EXPOSE_REP", "EI_EXPOSE_REP2"))
                .because("They are snippets",
                        In.loc("guru.nidi.ramltester.snippets*").ignoreAll())
                .because("it's class private and only used in 1 occasion",
                        In.loc("CheckerHelper$ResourceMatch").ignore("EQ_COMPARETO_USE_OBJECT_EQUALS"));
        return new FindBugsAnalyzer(AnalyzerConfig.maven().mainAndTest(), collector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final ViolationCollector collector = new ViolationCollector().minPriority(RulePriority.MEDIUM)
                .because("makes no sense",
                        In.everywhere().ignore("JUnitSpelling"))
                .because("does not understand constants (logger is NOT)",
                        In.everywhere().ignore("VariableNamingConventions"))
                .because("I don't agree",
                        In.everywhere().ignore(
                                "UncommentedEmptyMethodBody", "AvoidFieldNameMatchingMethodName", "AvoidFieldNameMatchingTypeName",
                                "AbstractNaming", "UncommentedEmptyConstructor", "SimplifyStartsWith", "EmptyMethodInAbstractClassShouldBeAbstract",
                                "AvoidSynchronizedAtMethodLevel", "UseStringBufferForStringAppends"),
                        In.loc("SecurityExtractor$SchemeFinder").ignore("ConfusingTernary"),
                        In.loc("UriComponents#getServer").ignore("NPathComplexity"))
                .because("arrays are only used internally",
                        In.locs("*Response", "*Request").ignore("MethodReturnsInternalArray", "ArrayIsStoredDirectly"))
                .because("not urgent and too many occasions",
                        In.everywhere().ignore(
                                "AvoidInstantiatingObjectsInLoops", "JUnitAssertionsShouldIncludeMessage", "JUnitTestContainsTooManyAsserts", "MethodArgumentCouldBeFinal"))
                .because("it's plain wrong",
                        In.loc("UsageCollector").ignore("ClassWithOnlyPrivateConstructorsShouldBeFinal"))
                .because("it's checked and correct",
                        In.locs("RelativeJsonSchemaAwareRamlDocumentBuilder", "MediaType", "ServletRamlMessageTest").ignore("CompareObjectsWithEquals"),
                        In.locs("JsRegex", "MediaType").ignore("PreserveStackTrace"),
                        In.locs("JsRegex","Usage").ignore("AvoidCatchingGenericException"),
                        In.classes(UriTest.class, ParameterCheckerTest.class, MediaTypeTest.class).ignore("JUnitTestsShouldIncludeAssert"))
                .because("it's style",
                        In.loc("RamlValidatorChecker").ignore("CollapsibleIfStatements"))
                .because("TODO",                 //TODO
                        In.locs("ParameterChecker", "Usage", "MediaType").ignore("GodClass"),
                        In.locs("VariableMatcher", "MediaType").ignore("CyclomaticComplexity", "NPathComplexity"),
                        In.loc("ContentNegotiationChecker").ignore("AvoidDeeplyNestedIfStmts"))
                .because("They are snippets",
                        In.loc("guru.nidi.ramltester.snippets*").ignoreAll())
                .because("is in test",
                        In.locs("*Test", "*Test$*").ignore("AvoidDuplicateLiterals", "SignatureDeclareThrowsException", "TooManyStaticImports", "AvoidDollarSigns"));
        return new PmdAnalyzer(AnalyzerConfig.maven().mainAndTest(), collector)
                .withRuleSets(basic(), braces(), design(), exceptions(), imports(), junit(),
                        optimizations(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().tooManyMethods(35),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 25))
                .analyze();
    }

    @Override
    protected CpdResult analyzeCpd() {
        final MatchCollector collector = new MatchCollector()
                .because("there's no common superclass",
                        In.locs("DelegatingServletOutputStream", "DelegatingWriter").ignoreAll())
                .because("TODO",                 //TODO
                        In.locs("SavingOutputStream", "DelegatingServletOutputStream", "DelegatingWriter").ignoreAll())
                .just(
                        In.loc("UsageCollector").ignore("static final UsageCollector"),
                        In.loc("SecurityExtractor").ignore("for (final SecurityScheme scheme : schemes)"),
                        In.everywhere().ignore("public boolean equals(Object o) {"))
                .because("Similar but not same",
                        In.locs("*Request", "*Response").ignoreAll(),
                        In.clazz(RamlHttpClient.class).ignoreAll());
        return new CpdAnalyzer(AnalyzerConfig.maven().main(), 35, collector).analyze();
    }
}
