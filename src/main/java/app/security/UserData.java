package app.security;

import app.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Base data for Authentication User
// Principle => object that stores Security data for authenticated user
@Data
@AllArgsConstructor
public class UserData implements UserDetails {

    private UUID userId;
    private String username;
    private String password;
    private UserRole role;
    //    private List<String> permissions;
    private boolean isAccountActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // examples
        // SimpleGrantedAuthority role = new SimpleGrantedAuthority("ROLE_" + "ADMIN");
        // SimpleGrantedAuthority permission1 = new SimpleGrantedAuthority("read_all_products");
        // SimpleGrantedAuthority permission2 = new SimpleGrantedAuthority("do_transfer");
        // SimpleGrantedAuthority permission3 = new SimpleGrantedAuthority("open_new_wallet");

        // check UserController => getUsers
        // @PreAuthorize("hasRole('ADMIN')") [search in 'ROLE_'] is when we used in 'UserData' - new SimpleGrantedAuthority("ROLE_" + role.name())
        // @PreAuthorize("hasAuthority('ADMIN')") when we use only ONE word - ex. new SimpleGrantedAuthority(role.name())
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name()); // ROLE_ADMIN || ROLE_USER

        // You can add (1 or several permissions OR 1 or several roles OR multiple permissions and roles)
        // List<SimpleGrantedAuthority> list = permissions.stream()
        //  .map(permission -> new SimpleGrantedAuthority(permission))
        //  .toList();

        return List.of(authority);
    }

    // Must have getPassword() and getUsername() for Spring Security !!!
    // The username can be a phone number, email, etc.
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isAccountActive;
    }

    @Override
    public boolean isEnabled() {
        return this.isAccountActive;
    }
}