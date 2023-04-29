package se.skaegg.discordbot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class SecurityConfiguration {

    @Value("${user.config.file}")
    private String usersFile;

    @Bean
    public UserDetailsService integrationUserDetailsService() throws IOException {
        Properties properties = new Properties();
        File file = new File(usersFile);
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        }
        return new InMemoryUserDetailsManager(properties);
    }

    @Bean
    public DaoAuthenticationConfigurer<AuthenticationManagerBuilder, UserDetailsService> configure(AuthenticationManagerBuilder auth) throws Exception {
        return auth.userDetailsService(integrationUserDetailsService()).passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests((authz) -> {
                    try {
                        authz
                                .requestMatchers(HttpMethod.GET).permitAll()
                                .anyRequest().hasRole("admin")
                                .and()
                                .httpBasic();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        return http.build();
    }

}
