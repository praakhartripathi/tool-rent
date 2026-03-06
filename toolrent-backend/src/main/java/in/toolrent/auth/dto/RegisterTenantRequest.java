package in.toolrent.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterTenantRequest {

    @NotBlank
    private String businessName;

    @NotBlank
    @Size(min = 3, max = 50, message = "Subdomain must be 3–50 characters")
    private String subdomain;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String fullName;
}
