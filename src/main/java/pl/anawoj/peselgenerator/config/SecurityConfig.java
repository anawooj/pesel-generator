package pl.anawoj.peselgenerator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfiguracja bezpieczeństwa aplikacji oparta na Spring Security.
 *
 * <p>Klasa definiuje:
 * <ul>
 *     <li>reguły autoryzacji dla poszczególnych endpointów,</li>
 *     <li>konfigurację logowania formularzowego,</li>
 *     <li>konfigurację wylogowania,</li>
 *     <li>bean odpowiedzialny za kodowanie haseł.</li>
 * </ul>
 *
 * <p>Konfiguracja opiera się na komponencie {@link SecurityFilterChain},
 * który zastępuje starszą klasę {@code WebSecurityConfigurerAdapter}.
 */
@Configuration
public class SecurityConfig {

    /**
     * Konfiguruje główny łańcuch filtrów Spring Security.
     *
     * <p>Reguły bezpieczeństwa obejmują:
     * <ul>
     *     <li>zezwolenie na dostęp publiczny do stron głównych, logowania,
     *         rejestracji oraz zasobów statycznych,</li>
     *     <li>wymaganie uwierzytelnienia dla pozostałych żądań,</li>
     *     <li>konfigurację logowania formularzowego z własną stroną logowania,</li>
     *     <li>konfigurację wylogowania z przekierowaniem na stronę główną.</li>
     * </ul>
     *
     * @param http obiekt konfiguracji HTTP dostarczany przez Spring Security
     * @return skonfigurowany {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/result", "/login",
                                "/css/**", "/js/**", "/register", "/register/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Bean odpowiedzialny za kodowanie haseł użytkowników.
     *
     * <p>Używany jest algorytm BCrypt, który zapewnia wysoki poziom bezpieczeństwa
     * dzięki mechanizmowi solenia i wielokrotnego haszowania.
     *
     * @return instancja {@link BCryptPasswordEncoder}
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}