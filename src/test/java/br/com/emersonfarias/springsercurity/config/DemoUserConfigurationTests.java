package br.com.emersonfarias.springsercurity.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class DemoUserConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DemoUserConfiguration.class)
            .withPropertyValues("app.security.username=config-test");

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
        });
    }
}
