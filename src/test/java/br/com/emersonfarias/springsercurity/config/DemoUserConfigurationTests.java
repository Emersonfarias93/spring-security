package br.com.emersonfarias.springsercurity.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class DemoUserConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DemoUserConfiguration.class)
            .withPropertyValues("app.security.username=config-test",
                    "app.security.admin-username=config-admin",
                    "app.security.admin-password=admin-test-only-password");

    @Test
    void shouldRejectMissingPassword() {
        contextRunner.withPropertyValues("app.security.password=").run(context ->
                assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "Defina DEMO_USER_PASSWORD antes de iniciar a aplicação."));
    }

    @Test
    void shouldRejectBlankUsername() {
        contextRunner.withPropertyValues("app.security.username=", "app.security.password=test-only-password")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "Defina um DEMO_USER_NAME não vazio."));
    }

    @Test
    void shouldStoreEncodedPasswordForConfiguredUser() {
        contextRunner.withPropertyValues("app.security.password=test-only-password").run(context -> {
            assertThat(context).hasNotFailed();
            UserDetails user = context.getBean(UserDetailsService.class).loadUserByUsername("config-test");

            assertThat(user.getPassword()).isNotEqualTo("test-only-password");
            assertThat(context.getBean(PasswordEncoder.class).matches("test-only-password", user.getPassword())).isTrue();
            assertThat(user.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

            UserDetails admin = context.getBean(UserDetailsService.class).loadUserByUsername("config-admin");
            assertThat(admin.getPassword()).isNotEqualTo("admin-test-only-password");
            assertThat(context.getBean(PasswordEncoder.class).matches("admin-test-only-password", admin.getPassword())).isTrue();
            assertThat(admin.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        });
    }

    @Test
    void shouldRejectMissingAdminPassword() {
        contextRunner.withPropertyValues("app.security.password=test-only-password", "app.security.admin-password=")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "Defina DEMO_ADMIN_PASSWORD antes de iniciar a aplicação."));
    }

    @Test
    void shouldRejectBlankAdminUsername() {
        contextRunner.withPropertyValues("app.security.password=test-only-password", "app.security.admin-username=")
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "Defina um DEMO_ADMIN_NAME não vazio."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"config-test", "CONFIG-TEST"})
    void shouldRejectDuplicateUsernamesIgnoringCase(String adminUsername) {
        contextRunner.withPropertyValues("app.security.password=test-only-password", "app.security.admin-username=" + adminUsername)
                .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                        "DEMO_USER_NAME e DEMO_ADMIN_NAME devem ser diferentes, inclusive sem distinguir maiúsculas."));
    }
}
