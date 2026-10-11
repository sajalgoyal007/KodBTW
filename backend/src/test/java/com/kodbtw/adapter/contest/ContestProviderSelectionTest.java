package com.kodbtw.adapter.contest;

import com.kodbtw.adapter.contest.codeforces.CodeforcesContestClientImpl;
import com.kodbtw.adapter.contest.codeforces.CodeforcesContestProvider;
import com.kodbtw.adapter.contest.fixture.FixtureContestProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class ContestProviderSelectionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ProviderConfiguration.class);

    @Test
    void defaultsToRealCodeforcesProviderWithoutCallingIt() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBeansOfType(ContestProvider.class)).hasSize(1);
            assertThat(context).hasSingleBean(CodeforcesContestProvider.class);
            assertThat(context).hasSingleBean(CodeforcesContestClientImpl.class);
            assertThat(context).doesNotHaveBean(FixtureContestProvider.class);
        });
    }

    @Test
    void fixtureModeLoadsOnlyFixtureProviderAndOmitsRealClient() {
        contextRunner.withPropertyValues(
                        "contest-calendar.provider=fixture",
                        "contest-calendar.fixture.scenario=multiple")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBeansOfType(ContestProvider.class)).hasSize(1);
                    assertThat(context).hasSingleBean(FixtureContestProvider.class);
                    assertThat(context).doesNotHaveBean(CodeforcesContestProvider.class);
                    assertThat(context).doesNotHaveBean(CodeforcesContestClientImpl.class);
                });
    }

    @Test
    void invalidProviderSelectionFailsStartupClearly() {
        contextRunner.withPropertyValues("contest-calendar.provider=unknown")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Invalid contest-calendar.provider. Supported values are 'codeforces' and 'fixture'.");
                });
    }

    @Test
    void invalidFixtureScenarioFailsStartupClearly() {
        contextRunner.withPropertyValues(
                        "contest-calendar.provider=fixture",
                        "contest-calendar.fixture.scenario=unknown")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Invalid contest-calendar.fixture.scenario. Supported values: multiple, empty, unavailable, stale.");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({
            ContestProviderSelectionValidator.class,
            CodeforcesContestClientImpl.class,
            CodeforcesContestProvider.class,
            FixtureContestProvider.class
    })
    static class ProviderConfiguration {
    }
}
