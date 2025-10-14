package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(min = 6, max = 26, message = "Username length must be between 6 and 24 symbols.")
    String username;

    @NotBlank
    @Size(min = 6, max = 6, message = "Password must be exactly 6 symbols.")
    String password;
}
