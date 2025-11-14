package app.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMethodSecurity
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .authorizeHttpRequests(matcher -> matcher
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/register", "/error").permitAll()
                        // This condition is in the ...Controller, but we can set here
                        // .requestMatchers("/admin-panel").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        // Default Parameters ("username" & "password"), but we can change this String with our own
                        //.usernameParameter("username")
                        //.passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                        // todo For CSRF protection
                        // Use the default POST /logout and remove the GET matcher
                        // Post the logout with the CSRF token:
                        // <form th:action="@{/logout}" method="post">
                        // TOKEN in Fragment - reusable todo !!!
                        //  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                        //  <button type="submit">Logout</button>
                        // </form>

                        // How to verify CSRF is enabled
                        // - Submit a POST to any protected endpoint without a CSRF token;
                        //      you should get 403 Forbidden with an “Invalid CSRF token” message.
                        // - In logs (DEBUG), you’ll see CsrfFilter in the filter chain.
                        // - In a test, MockMvc will need with(csrf()) for state-changing requests.
                );

        return httpSecurity.build();
    }
}
