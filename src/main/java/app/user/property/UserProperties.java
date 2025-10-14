package app.user.property;

import app.user.model.UserCountry;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "users")
public class UserProperties {

    private DefaultUser defaultUser;

    private String testProperty;

    @Data
    public static class DefaultUser{

        private String username;

        private String password;

        private UserCountry country;
    }

//    @PostConstruct
//    public void test() {
//        System.out.println();
//    }
}
