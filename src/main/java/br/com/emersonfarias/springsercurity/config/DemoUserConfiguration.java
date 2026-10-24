package br.com.emersonfarias.springsercurity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class DemoUserConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(@Value("${app.security.username}") String username,
                                         @Value("${app.security.password}") String password,
                                         PasswordEncoder passwordEncoder) {
        Assert.hasText(username, "Defina um DEMO_USER_NAME não vazio.");
        Assert.hasText(password, "Defina DEMO_USER_PASSWORD antes de iniciar a aplicação.");

        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());
    }
}
